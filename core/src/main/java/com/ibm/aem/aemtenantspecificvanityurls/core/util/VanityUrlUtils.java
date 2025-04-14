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
package com.ibm.aem.aemtenantspecificvanityurls.core.util;

import com.day.text.Text;
import org.apache.sling.api.resource.path.PathBuilder;

public final class VanityUrlUtils {

    private VanityUrlUtils() {
        // empty
    }

    /**
     * Prepends the specified prefix to the given vanity path if it is not already a descendant of the prefix.
     * <p>Examples:</p>
     * <pre>
     * prependPrefixIfMissing("wow", "/us/en")          = "/us/en/wow"
     * prependPrefixIfMissing("wow", "/us/en/")         = "/us/en/wow"
     * prependPrefixIfMissing("/wow", "/us/en")         = "/us/en/wow"
     * prependPrefixIfMissing("/wow", "/us/en/")        = "/us/en/wow"
     * prependPrefixIfMissing("/us/en/wow", "/us/en")   = "/us/en/wow"
     * </pre>
     *
     * @param vanityPath the vanity path to check and potentially prepend the prefix to
     * @param prefix the prefix to prepend if the vanity path is not already under it
     * @return the resulting path with the prefix prepended if necessary
     */
    public static String prependPrefixIfMissing(final String vanityPath, final String prefix) {
        if (Text.isDescendant(prefix, vanityPath)) {
            return vanityPath;
        }
        return new PathBuilder(prefix).append(vanityPath).toString();
    }

}
