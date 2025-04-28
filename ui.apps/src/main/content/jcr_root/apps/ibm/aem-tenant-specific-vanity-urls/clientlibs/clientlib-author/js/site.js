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

window.tsvu_prefix = window.tsvu_prefix || {};

/**
 * Setup of form and field handlers.
 */
window.tsvu_prefix.init = function () {
    if (window.location.pathname === '/mnt/overlay/wcm/core/content/sites/properties.html') {
        const prefixUrl = window.tsvu_prefix.getTsvuServletUrl();
        if (prefixUrl) {
            fetch(prefixUrl)
                .then((response) => response.json())
                .then((data) => {
                    if (data.prefix && (data.prefix.length > 0)) {
                        window.tsvu_prefix.clearPrefixWhenLoaded(data.prefix);
                        window.tsvu_prefix.addSaveHandler(data.prefix, data.toLowerCase);
                        window.tsvu_prefix.addFieldValidator();
                    }
                });
        }
    }
}

/**
 * Constructs a URL for the `TenantSpecificVanityUrlServlet` with optional parameters.
 *
 * @param params An optional object representing query parameters to append to the URL.
 */
window.tsvu_prefix.getTsvuServletUrl = function (params) {
    const urlParams = new URLSearchParams(window.location.search);
    const item = urlParams.get('item');
    if (item && item.startsWith("/content")) {
        let url = item + ".tsvu.json";
        if (params) {
            url += "?" + new URLSearchParams(params).toString();
        }
        return url;
    }
}

/**
 * Clears the prefix from existing vanity URL fields once the content is loaded.
 *
 * @param prefix vanity prefix
 */
window.tsvu_prefix.clearPrefixWhenLoaded = function(prefix) {
    const fields = window.tsvu_prefix.findInputFields();
    if (fields.length > 0) {
        window.tsvu_prefix.clearPrefix(prefix);
    }
    else {
        // probably, content is not yet loaded, wait
        $(document).on("foundation-contentloaded", function(e) {
            window.tsvu_prefix.clearPrefix(prefix);
        });
    }
}

/**
 * Returns a list of vanity URL input fields.
 *
 * @return fields
 */
window.tsvu_prefix.findInputFields = function() {
    return document.querySelectorAll('input[name="./sling:vanityPath"]');
}

/**
 * Clears the prefix from existing vanity URL fields.
 *
 * @param prefix vanity prefix
 */
window.tsvu_prefix.clearPrefix = function(prefix) {
    const fields = window.tsvu_prefix.findInputFields();
    fields.forEach(function (currentValue) {
        if (currentValue.value && currentValue.value.startsWith(prefix)) {
            currentValue.value = currentValue.value.substring(prefix.length);
        }
    });
}

/**
 * Registers the form save handler and readds the prefixes before form is saved.
 *
 * @param prefix vanity prefix
 * @param toLowerCase convert the value to lower-case
 */
window.tsvu_prefix.addSaveHandler = function (prefix, toLowerCase) {
    $(window).adaptTo("foundation-registry").register("foundation.form.submit", {
        selector: '*',
        handler: function() {
            const fields = window.tsvu_prefix.findInputFields();
            fields.forEach(function (currentValue) {
                if (currentValue.value && !currentValue.value.startsWith(prefix)) {
                    if (toLowerCase) {
                        currentValue.value = currentValue.value.toLowerCase();
                    }
                    currentValue.value = window.tsvu_prefix.prependPrefixIfMissing(
                        currentValue.value, prefix);
                }
            });
            return {};
        }
    });
}

/**
 * Prepends the specified prefix to the given vanity path if it is not already a descendant of the
 * prefix.
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
window.tsvu_prefix.prependPrefixIfMissing = function (vanityPath, prefix) {
    if (vanityPath.startsWith(prefix)) {
        return vanityPath;
    }
    return prefix.trim().replace(/\/+$/, "") + "/" +
        vanityPath.trim().replace(/^\/+/, "");
}

/**
 * Registers backend validation for the vanity URL input fields.
 */
window.tsvu_prefix.addFieldValidator = function () {
    window.tsvu_prefix.BackendValidation.registerValidator({
        selector: 'input[name="./sling:vanityPath"]',
        ignoreEmpty: true,
        checkValidity: async function (el, controller) {
            try {
                const validationUrl = window.tsvu_prefix.getTsvuServletUrl({
                    cmd: "unique",
                    path: el.value
                });
                if (!validationUrl) {
                    throw new Error("Can't build validation url");
                }
                const response = await fetch(validationUrl, {
                    signal: controller.signal
                });
                if (!response.ok) {
                    throw new Error(`Response status: ${response.status}`);
                }
                return await response.json();
            } catch (e) {
                return {
                    error: true,
                    message: e.message
                };
            }
        },
        validationMessage(el, result) {
            if (result.error) {
                return Granite.I18n.get("Error checking for unique value");
            }

            if (!result.valid) {
                const conflictLink = createConflictLink(result.conflicts[0]);
                return Granite.I18n.get("The value is already used: ") + conflictLink;
            }
        }
    });

    $(document).one("foundation-contentloaded", function(e) {
        window.tsvu_prefix.findInputFields().forEach(el => {
            window.tsvu_prefix.BackendValidation.validateField(el)
        });
    });

    function createConflictLink(conflict) {
        const editorUrl = Granite.HTTP.externalize("/mnt/overlay/wcm/core/content/sites/properties.html");
        const href = `${editorUrl}?${new URLSearchParams({item: conflict.path})}`;
        return `<a href="${href}" target="_blank">${conflict.title}</a>`;
    }
}

window.tsvu_prefix.init();
