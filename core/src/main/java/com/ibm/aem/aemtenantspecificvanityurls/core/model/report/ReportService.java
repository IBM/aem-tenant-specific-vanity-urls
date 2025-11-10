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

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.NameConstants;
import com.ibm.aem.aemtenantspecificvanityurls.core.exceptions.AtsvuException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Searches for vanity entries for the report.
 *
 * @author Roland Gruber
 */
@Component(service = ReportService.class)
public class ReportService {

    public static final String ORDER_ASC = "asc";
    public static final String ORDER_DESC = "desc";
    public static final String ORDER_BY_PATH = "path";
    public static final String ORDER_BY_VANITY_PATH = "vanityUrl";

    /**
     * Order types
     */
    public enum ORDER {
        ASC,
        DESC;

        public static ORDER parse(String value) {
            if (ORDER_ASC.equals(value)) {
                return ASC;
            }
            if (ORDER_DESC.equals(value)) {
                return DESC;
            }
            return null;
        }

    }

    /**
     * Order attributes
     */
    public enum ORDER_ATTR {
        PATH,
        VANITY_PATH;

        public static ORDER_ATTR parse(String value) {
            if (ORDER_BY_PATH.equals(value)) {
                return PATH;
            }
            if (ORDER_BY_VANITY_PATH.equals(value)) {
                return VANITY_PATH;
            }
            return null;
        }
    }

    @Reference
    private QueryBuilder queryBuilder;

    /**
     * Returns a list of vanity entries.
     *
     * @param offset           offset
     * @param limit            search limit
     * @param orderAttr        order attribute
     * @param order            order direction
     * @param resolver         resource resolver
     * @return entries
     * @throws AtsvuException error during search
     */
    public List<ReportEntry> getVanityEntries(int offset, int limit, ORDER_ATTR orderAttr, ORDER order, ResourceResolver resolver) throws AtsvuException {
        Map<String, Object> predicates = new HashMap<>();
        predicates.put("path", "/content");
        predicates.put("property", NameConstants.PN_SLING_VANITY_PATH);
        predicates.put("property.operation", "exists");
        if (orderAttr == ORDER_ATTR.PATH) {
            predicates.put("orderby", "@jcr:path");
        }
        else {
            predicates.put("orderby", "@sling:vanityPath");
        }
        if (ORDER.DESC == order) {
            predicates.put("orderby.sort", "desc");
        }
        PredicateGroup predicateGroup = PredicateGroup.create(predicates);
        Query query = queryBuilder.createQuery(predicateGroup, resolver.adaptTo(Session.class));
        if (limit != 0) {
            query.setHitsPerPage(limit);
        }
        if (offset != 0) {
            query.setStart(offset);
        }
        List<ReportEntry> entries = new ArrayList<>();
        try {
            SearchResult result = query.getResult();
            List<Hit> hits = result.getHits();
            for (Hit hit : hits) {
                Resource resource = hit.getResource();
                Resource pageResource = resource.getParent();
                String[] vanities = resource.getValueMap().get(NameConstants.PN_SLING_VANITY_PATH, String[].class);
                for (String vanity : vanities) {
                    ReportEntry entry = new ReportEntry();
                    entry.setVanityUrl(vanity);
                    entry.setPagePath(pageResource.getPath());
                    entries.add(entry);
                }
            }
        }
        catch (RepositoryException e) {
            throw new AtsvuException("Vanity search failed", e);
        }
        return entries;
    }
}
