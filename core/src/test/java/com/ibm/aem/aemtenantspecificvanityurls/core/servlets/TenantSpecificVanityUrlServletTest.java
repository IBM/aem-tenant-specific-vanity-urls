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

import com.ibm.aem.aemtenantspecificvanityurls.core.caconfig.TenantSpecificVanityUrlConfig;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests TenantSpecificVanityUrlServlet
 */
@ExtendWith(MockitoExtension.class)
public class TenantSpecificVanityUrlServletTest {

    public static final String MYPREFIX = "myprefix";
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

    @InjectMocks
    private TenantSpecificVanityUrlServlet servlet;

    @BeforeEach
    void setup() throws IOException {
        when(request.getResource()).thenReturn(resource);
        when(resource.adaptTo(ConfigurationBuilder.class)).thenReturn(builder);
        when(builder.as(TenantSpecificVanityUrlConfig.class)).thenReturn(config);
        when(config.prefix()).thenReturn(MYPREFIX);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void doGet() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(writer).write("{\"prefix\":\"" + MYPREFIX + "\",\"toLowerCase\":false}");
    }

}
