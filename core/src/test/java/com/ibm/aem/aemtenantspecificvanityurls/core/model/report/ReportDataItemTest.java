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

import com.ibm.aem.aemtenantspecificvanityurls.core.caconfig.TenantSpecificVanityUrlConfig;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests ReportDataItem
 *
 * @author Roland Gruber
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReportDataItemTest {

    private static final String PREFIX = "/content/site/";

    private static final String PAGE_PATH = "/content/site/x/y/z/page";

    private static final String MAPPED_PATH = "/x/y/z/page";

    private static final String VANITY_PATH = "/content/site/wow";

    private static final String VANITY_SHORT = "wow";

    @Mock
    private Resource resource;

    @Mock
    private ValueMap vm;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private ConfigurationBuilder configurationBuilder;

    @Mock
    TenantSpecificVanityUrlConfig config;

    @InjectMocks
    private ReportDataItem item;

    @BeforeEach
    void setup() {
        when(resource.getValueMap()).thenReturn(vm);
        ReportEntry reportEntry = new ReportEntry();
        reportEntry.setVanityUrl(VANITY_PATH);
        reportEntry.setPagePath(PAGE_PATH);
        when(vm.get(ReportDataSource.ATTR_REPORT, ReportEntry.class)).thenReturn(reportEntry);
        when(resource.adaptTo(ConfigurationBuilder.class)).thenReturn(configurationBuilder);
        when(configurationBuilder.as(TenantSpecificVanityUrlConfig.class)).thenReturn(config);
        when(config.prefix()).thenReturn(PREFIX);
        when(resource.getResourceResolver()).thenReturn(resolver);
        when(resolver.map(PAGE_PATH)).thenReturn(MAPPED_PATH);
    }

    @Test
    void getVanityUrl() {
        item.setup();

        assertEquals(VANITY_SHORT, item.getVanityUrl());
    }

    @Test
    void getPagePath() {
        item.setup();

        assertEquals(PAGE_PATH, item.getPagePath());
    }

    @Test
    void getMappedPath() {
        item.setup();

        assertEquals(MAPPED_PATH, item.getMappedPath());
    }

}
