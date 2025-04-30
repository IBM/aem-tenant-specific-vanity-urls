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

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.ibm.aem.aemtenantspecificvanityurls.core.caconfig.TenantSpecificVanityUrlConfig;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static com.ibm.aem.aemtenantspecificvanityurls.core.servlets.TenantSpecificVanityUrlServlet.*;
import static org.mockito.Mockito.*;

/**
 * Tests TenantSpecificVanityUrlServlet
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TenantSpecificVanityUrlServletTest {

    public static final String MYPREFIX = "/myprefix";
    public static final String CURRENT_PAGE_PATH = "/content/wknd/us/en/adventures/yosemite-backpacking";
    public static final String CONFLICTING_PAGE_PATH = "/content/wknd/us/en/adventures/ski-touring-mont-blanc";
    public static final String CONFLICTING_PAGE_TITLE = "Ski Touring Mont Blanc";

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    private Resource resource;

    @Mock
    private ConfigurationBuilder builder;

    @Mock
    private TenantSpecificVanityUrlConfig config;

    @Mock
    private PrintWriter writer;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private ConfigurationResourceResolver configurationResourceResolver;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private Session session;

    @Mock
    private Query query;

    @Mock
    private SearchResult searchResult;

    @Mock
    private Page currentPage;

    @InjectMocks
    private TenantSpecificVanityUrlServlet servlet;

    @BeforeEach
    void setup() throws IOException {
        when(request.getResource()).thenReturn(resource);
        when(resource.adaptTo(ConfigurationBuilder.class)).thenReturn(builder);
        when(builder.as(TenantSpecificVanityUrlConfig.class)).thenReturn(config);
        when(config.prefix()).thenReturn(MYPREFIX);
        when(response.getWriter()).thenReturn(writer);
        when(configurationResourceResolver.getAllContextPaths(resource)).thenReturn(Arrays.asList(MYPREFIX));
    }

    @Test
    void doGet() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(writer).write("{\"prefix\":\"" + MYPREFIX + "\",\"toLowerCase\":false}");
    }

    @Test
    void testUniqueVanityUrl() throws ServletException, IOException {
        when(resource.adaptTo(Page.class)).thenReturn(currentPage);
        when(request.getParameter(RP_COMMAND)).thenReturn(CMD_CHECK_UNIQUENESS);
        when(request.getParameter(RP_VANITY_PATH)).thenReturn("wow");

        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(session);
        when(queryBuilder.createQuery(Mockito.any(), Mockito.eq(session))).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getResources()).thenReturn(IteratorUtils.arrayIterator());

        servlet.doGet(request, response);

        verify(writer).write("{" +
                "\"valid\":true," +
                "\"prefix\":\"" + MYPREFIX + "\"," +
                "\"vanityPath\":\"" + MYPREFIX + "/wow\"" +
                "}");
    }

    @Test
    void testUniqueVanityUrlWithLowerCase() throws ServletException, IOException {
        when(config.toLowerCase()).thenReturn(true);

        when(resource.adaptTo(Page.class)).thenReturn(currentPage);
        when(request.getParameter(RP_COMMAND)).thenReturn(CMD_CHECK_UNIQUENESS);
        when(request.getParameter(RP_VANITY_PATH)).thenReturn("WOW");

        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(session);
        when(queryBuilder.createQuery(Mockito.any(), Mockito.eq(session))).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getResources()).thenReturn(IteratorUtils.arrayIterator());

        servlet.doGet(request, response);

        verify(writer).write("{" +
                "\"valid\":true," +
                "\"prefix\":\"" + MYPREFIX + "\"," +
                "\"vanityPath\":\"" + MYPREFIX + "/wow\"" +
                "}");
    }

    @Test
    void testDuplicateVanityUrl() throws ServletException, IOException {
        when(resource.adaptTo(Page.class)).thenReturn(currentPage);
        when(currentPage.getPath()).thenReturn(CURRENT_PAGE_PATH);
        when(request.getParameter(RP_COMMAND)).thenReturn(CMD_CHECK_UNIQUENESS);
        when(request.getParameter(RP_VANITY_PATH)).thenReturn("wow");

        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(session);
        when(queryBuilder.createQuery(Mockito.any(), Mockito.eq(session))).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        Page conflictingPage = mock(Page.class);
        when(conflictingPage.getPath()).thenReturn(CONFLICTING_PAGE_PATH);
        when(conflictingPage.getTitle()).thenReturn(CONFLICTING_PAGE_TITLE);
        Resource conflictingPageResource = mock(Resource.class);
        when(conflictingPageResource.adaptTo(Page.class)).thenReturn(conflictingPage);
        when(searchResult.getResources()).thenReturn(IteratorUtils.arrayIterator(new Resource[]{conflictingPageResource}));

        servlet.doGet(request, response);

        verify(writer).write("{" +
                "\"valid\":false," +
                "\"prefix\":\"" + MYPREFIX + "\"," +
                "\"vanityPath\":\"" + MYPREFIX + "/wow\"," +
                "\"conflicts\":[" +
                "{\"path\":\"" + CONFLICTING_PAGE_PATH + "\",\"title\":\"" + CONFLICTING_PAGE_TITLE + "\"}" +
                "]}");
    }

}
