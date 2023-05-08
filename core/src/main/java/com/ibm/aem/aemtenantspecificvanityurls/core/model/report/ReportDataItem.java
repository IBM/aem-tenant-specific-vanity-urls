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
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;

/**
 * Model class for a single report item.
 *
 * @author Roland Gruber
 */
@Model(adaptables = Resource.class)
public class ReportDataItem {

    @SlingObject
    private Resource resource;

    protected ReportEntry reportEntry = null;

    @PostConstruct
    public void setup() {
        reportEntry = resource.getValueMap().get(ReportDataSource.ATTR_REPORT, ReportEntry.class);
    }

    /**
     * Returns the vanity URL (without prefix).
     *
     * @return date
     */
    public String getVanityUrl() {
        ConfigurationBuilder configBuilder = resource.adaptTo(ConfigurationBuilder.class);
        TenantSpecificVanityUrlConfig config = configBuilder.as(TenantSpecificVanityUrlConfig.class);
        String prefix = config.prefix();
        String url = reportEntry.getVanityUrl();
        if (!StringUtils.isEmpty(prefix) && url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return url;
    }

    /**
     * Returns mapped path.
     *
     * @return date
     */
    public String getMappedPath() {
        return resource.getResourceResolver().map(reportEntry.getPagePath());
    }

    /**
     * Returns the page path.
     *
     * @return path
     */
    public String getPagePath() {
        return reportEntry.getPagePath();
    }

}
