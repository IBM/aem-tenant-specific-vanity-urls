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
        const urlParams = new URLSearchParams(window.location.search);
        const item = urlParams.get('item');
        if (item && item.startsWith("/content")) {
            const prefixUrl = item + ".tsvu_prefix.json";
            fetch(prefixUrl)
                .then((response) => response.json())
                .then((data) => {
                    if (data.prefix && (data.prefix.length > 0)) {
                        window.tsvu_prefix.clearPrefixWhenLoaded(data.prefix);
                        window.tsvu_prefix.addSaveHandler(data.prefix, data.toLowerCase);
                    }
                });
        }
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
                    currentValue.value = prefix + currentValue.value;
                }
            });
            return {};
        }
    });
}

window.tsvu_prefix.init();
