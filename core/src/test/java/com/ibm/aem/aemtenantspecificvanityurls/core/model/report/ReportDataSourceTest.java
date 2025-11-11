/**
 * Copyright 2023 IBM iX
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
package com.ibm.aem.aemtenantspecificvanityurls.core.model.report;

import com.adobe.granite.ui.components.ds.DataSource;
import com.ibm.aem.aemtenantspecificvanityurls.core.exceptions.AtsvuException;
import org.apache.http.client.fluent.Request;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ibm.aem.aemtenantspecificvanityurls.core.model.report.ReportDataSource.ATTR_REPORT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests ReportDataSource
 *
 * @author Roland Gruber
 */
@ExtendWith(MockitoExtension.class)
class ReportDataSourceTest {

    private static final String PAGE1 = "/content/site/x/y/z/p1";
    private static final String PAGE2 = "/content/site/x/y/z/p2";

    private static final String VANITY1 = "/content/site/v1";
    private static final String VANITY2 = "/content/site/v2";

    @Mock
    private ReportService reportService;

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private RequestPathInfo requestPathInfo;

    @Mock
    private ResourceResolver resolver;

    @InjectMocks
    private ReportDataSource reportDataSource;

    private List<ReportEntry> reportList = new ArrayList<>();

    @BeforeEach
    void setup() throws AtsvuException {
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getResourceResolver()).thenReturn(resolver);
        when(requestPathInfo.getSelectors()).thenReturn(new String[] {"200", "100"});
        when(reportService.getVanityEntries(200, 101, ReportService.ORDER_ATTR.PATH, ReportService.ORDER.ASC, resolver)).thenReturn(reportList);
    }

    @Test
    void requestAttribute() {
        ReportEntry e1 = new ReportEntry();
        e1.setPagePath(PAGE1);
        e1.setVanityUrl(VANITY1);
        reportList.add(e1);
        ReportEntry e2 = new ReportEntry();
        e2.setPagePath(PAGE2);
        e2.setVanityUrl(VANITY2);
        reportList.add(e2);

        reportDataSource.setup();

        ArgumentCaptor<DataSource> argumentCaptor = ArgumentCaptor.forClass(DataSource.class);
        verify(request).getRequestPathInfo();
        verify(request).getRequestParameter("sortDir");
        verify(request).getRequestParameter("sortName");
        verify(request).setAttribute(Mockito.eq(DataSource.class.getName()), argumentCaptor.capture());
        DataSource dataSource = argumentCaptor.getValue();
        Iterator<Resource> it = dataSource.iterator();
        assertTrue(it.hasNext());
        Resource res1 = it.next();
        assertEquals(e1, res1.getValueMap().get(ATTR_REPORT));
        assertTrue(it.hasNext());
        Resource res2 = it.next();
        assertEquals(e2, res2.getValueMap().get(ATTR_REPORT));
        assertFalse(it.hasNext());
    }

}
