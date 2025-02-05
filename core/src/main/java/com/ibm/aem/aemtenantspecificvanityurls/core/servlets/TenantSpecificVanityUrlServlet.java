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

import com.google.gson.JsonObject;
import com.ibm.aem.aemtenantspecificvanityurls.core.caconfig.TenantSpecificVanityUrlConfig;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

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
        "sling.servlet.selectors=tsvu_prefix",
        "sling.servlet.resourceTypes=sling/servlet/default"})
public class TenantSpecificVanityUrlServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        Resource resource = request.getResource();
        ConfigurationBuilder configurationBuilder = resource.adaptTo(ConfigurationBuilder.class);
        TenantSpecificVanityUrlConfig config = configurationBuilder.as(TenantSpecificVanityUrlConfig.class);
        JsonObject json = new JsonObject();
        json.addProperty("prefix", config.prefix());
        json.addProperty("toLowerCase", config.toLowerCase());
        response.getWriter().write(json.toString());
    }

}
