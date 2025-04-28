/**
 * Copyright 2023 - 2025 IBM iX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ibm.aem.aemtenantspecificvanityurls.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.aem.aemtenantspecificvanityurls.core.caconfig.TenantSpecificVanityUrlConfig;
import com.ibm.aem.aemtenantspecificvanityurls.core.util.VanityUrlUtils;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Servlet to provide prefix for vanity URLs.
 *
 * @author Roland Gruber
 */
@Component(service = Servlet.class,
immediate = true,
property = {
        "sling.servlet.methods=GET",
        "sling.servlet.extensions=json",
        "sling.servlet.selectors=tsvu",
        "sling.servlet.resourceTypes=sling/servlet/default"},
configurationPolicy = ConfigurationPolicy.REQUIRE)
public class TenantSpecificVanityUrlServlet extends SlingSafeMethodsServlet {

    public static final String RP_COMMAND = "cmd";
    public static final String CMD_LOAD_CONFIG = "cfg";
    public static final String CMD_CHECK_UNIQUENESS = "unique";

    public static final String RP_VANITY_PATH = "path";

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        String cmd = StringUtils.defaultIfBlank(request.getParameter(RP_COMMAND), CMD_LOAD_CONFIG);

        if (StringUtils.equals(cmd, CMD_LOAD_CONFIG)) {
            doLoadConfigCommand(request, response);
        } else if (StringUtils.equals(cmd, CMD_CHECK_UNIQUENESS)) {
            doCheckUniquenessCommand(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported command: " + cmd);
        }
    }

    private void doLoadConfigCommand(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        TenantSpecificVanityUrlConfig config = resolveVanityUrlConfig(request);

        JsonObject json = new JsonObject();
        json.addProperty("prefix", config.prefix());
        json.addProperty("toLowerCase", config.toLowerCase());

        sendResponse(response, json);
    }

    private void doCheckUniquenessCommand(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        TenantSpecificVanityUrlConfig config = resolveVanityUrlConfig(request);

        String vanityPath = request.getParameter(RP_VANITY_PATH);
        if (StringUtils.isBlank(vanityPath)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Vanity path required");
            return;
        }
        vanityPath = VanityUrlUtils.prependPrefixIfMissing(vanityPath, config.prefix());
        if (config.toLowerCase()) {
            vanityPath = StringUtils.lowerCase(vanityPath);
        }

        ResourceResolver resolver = request.getResourceResolver();
        Iterator<Page> conflictingPages = filterCurrentPage(
                findPagesByVanityPath(config.prefix(), vanityPath, resolver),
                request.getResource().adaptTo(Page.class)
        );

        JsonObject result = new JsonObject();
        result.addProperty("prefix", config.prefix());
        result.addProperty("vanityPath", vanityPath);
        result.addProperty("valid", !conflictingPages.hasNext());
        if (conflictingPages.hasNext()) {
            JsonArray conflicts = new JsonArray();
            while (conflictingPages.hasNext()) {
                Page page = conflictingPages.next();
                JsonObject conflict = new JsonObject();
                conflict.addProperty("path", page.getPath());
                conflict.addProperty("title", StringUtils.defaultIfBlank(page.getTitle(), page.getName()));
                conflicts.add(conflict);
            }
            result.add("conflicts", conflicts);
        }

        sendResponse(response, result);
    }

    private TenantSpecificVanityUrlConfig resolveVanityUrlConfig(SlingHttpServletRequest request) {
        Resource resource = request.getResource();
        ConfigurationBuilder configurationBuilder = resource.adaptTo(ConfigurationBuilder.class);
        return configurationBuilder.as(TenantSpecificVanityUrlConfig.class);
    }

    private Iterator<Page> findPagesByVanityPath(String contentPath, String vanityPath, ResourceResolver resolver) {
        Map<String, String> map = new HashMap<>();
        map.put("path", contentPath);
        map.put("type", "cq:Page");
        map.put("1_property", "jcr:content/sling:vanityPath");
        map.put("1_property.value", vanityPath);

        Session session = resolver.adaptTo(Session.class);
        Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
        query.setStart(0);
        query.setHitsPerPage(2);

        SearchResult result = query.getResult();
        return new TransformIterator<>(result.getResources(), resource -> resource.adaptTo(Page.class));
    }

    private Iterator<Page> filterCurrentPage(Iterator<Page> pages, Page currentPage) {
        if (currentPage != null) {
            return new FilterIterator<>(pages, page -> !StringUtils.equals(page.getPath(), currentPage.getPath()));
        }
        return pages;
    }

    private void sendResponse(SlingHttpServletResponse response, JsonObject result) throws IOException {
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(result.toString());
    }

}
