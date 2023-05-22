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

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.NameConstants;
import com.ibm.aem.aemtenantspecificvanityurls.core.exceptions.AtsvuException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests ReportService.
 *
 * @author Roland Gruber
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final String PATH1 = "/content/site/x/y/z/page1";
    private static final String PATH2 = "/content/site/x/y/z/page2";
    private static final String VANITY1 = "/content/site/v1";
    private static final String VANITY2 = "/content/site/v2";

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private Session session;

    @Mock
    private Query query;

    @Mock
    private SearchResult searchResult;

    @Mock
    private Hit hit1;
    @Mock
    private Hit hit2;

    @Mock
    private Resource resource1;
    @Mock
    private Resource resource2;

    @Mock
    private ValueMap vm1;
    @Mock
    private ValueMap vm2;

    @InjectMocks
    private ReportService service;

    @BeforeEach
    void setup() throws RepositoryException {
        when(resolver.adaptTo(Session.class)).thenReturn(session);
        when(queryBuilder.createQuery(Mockito.any(), Mockito.eq(session))).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(hit1.getResource()).thenReturn(resource1);
        when(hit2.getResource()).thenReturn(resource2);
        when(resource1.getValueMap()).thenReturn(vm1);
        when(resource2.getValueMap()).thenReturn(vm2);
        when(resource1.getParent()).thenReturn(resource1);
        when(resource2.getParent()).thenReturn(resource2);
        when(resource1.getPath()).thenReturn(PATH1);
        when(resource2.getPath()).thenReturn(PATH2);
        when(vm1.get(NameConstants.PN_SLING_VANITY_PATH, String.class)).thenReturn(VANITY1);
        when(vm2.get(NameConstants.PN_SLING_VANITY_PATH, String.class)).thenReturn(VANITY2);
    }

    @Test
    void getVanityEntries() throws AtsvuException {
        List<Hit> hits = Arrays.asList(hit1, hit2);
        when(searchResult.getHits()).thenReturn(hits);

        List<ReportEntry> entries = service.getVanityEntries(0, 100, ReportService.ORDER_ATTR.PATH, ReportService.ORDER.ASC, resolver);

        assertEquals(2, entries.size());
        assertEquals(PATH1, entries.get(0).getPagePath());
        assertEquals(PATH2, entries.get(1).getPagePath());
        assertEquals(VANITY1, entries.get(0).getVanityUrl());
        assertEquals(VANITY2, entries.get(1).getVanityUrl());
    }

}
