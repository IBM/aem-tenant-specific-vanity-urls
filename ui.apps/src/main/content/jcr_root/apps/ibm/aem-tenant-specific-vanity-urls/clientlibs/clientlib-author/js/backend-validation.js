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

(function (window, Granite, $) {
    "use strict";

    const MSG_IS_LOADING = "loading";

    const registry = $(window).adaptTo("foundation-registry")
    const defaultValidator = registry.get("foundation.validation.validator")
        .filter(v => v.selector === "*")[0];
    const defaultShow = defaultValidator.show;
    const defaultClear = defaultValidator.clear;

    /**
     * Registers a custom asynchronous form validator using the Granite UI foundation validation
     * framework.
     *
     * Note: It's recommended to trigger an initial validation on page load, to avoid issues when
     * submitting the form.
     *
     * @param {Object} options - Configuration options for the validator.
     * @param {string} options.selector - CSS selector string to target input elements for validation.
     * @param {boolean} [options.ignoreEmpty=false] - If true, validation is skipped when the input
     * value is blank.
     * @param {function(HTMLElement, AbortController): Promise<any>} options.checkValidity - Asynchronous
     *        function that validates the input. It should return a promise that resolves with the
     *        validation result.
     * @param {function(HTMLElement, any): string} options.validationMessage - Function that returns a
     *        validation message string based on the validation result.
     * @param {function(HTMLElement, string, Object)} [options.show] - Optional function to show a
     *        validation error. Defaults to a standard error handler.
     * @param {function(HTMLElement, Object)} [options.clear] - Optional function to clear a validation
     *        state. Defaults to a standard clear handler.
     *
     * @example
     * registerValidator({
     *   selector: 'input[name="./myInput"]',
     *   checkValidity: async function(el, controller) {
     *       const response = await fetch(validationUrl, {signal: controller.signal});
     *       return await response.json();
     *   },
     *   validationMessage: function(el, result) {
     *       return result.valid ? "" : result.message;
     *   }
     * });
     *
     * $(document).one("foundation-contentloaded", function(e) {
     *   inputFields.forEach(el => {
     *     BackendValidation.validateField(el)
     *   });
     * });
     */
    function registerValidator(options) {
        registry.register("foundation.validation.validator", {
            selector: options.selector,
            validate: function (el) {
                const context = el.validationContext = el.validationContext || {};

                if (options.ignoreEmpty && el.value.trim().length === 0) {
                    return;
                }

                if (context.result && context.value === el.value) {
                    return options.validationMessage(el, context.result);
                }

                if (context.controller && context.value !== el.value) {
                    context.controller.abort();
                    delete context.controller;
                }

                if (!context.controller) {
                    context.controller = new AbortController();
                    context.value = el.value;
                    delete context.result;
                    options.checkValidity(el, context.controller).then(result => {
                        context.result = result;
                        delete context.controller;
                        validateField(el);
                    });
                }

                return MSG_IS_LOADING;
            },
            show: function (el, message, ctx) {
                if (message === MSG_IS_LOADING) {
                    el.classList.add("is-loading");
                    options.clear ? options.clear(el, ctx) : defaultClear(el, ctx);
                } else {
                    options.show ? options.show(el, message, ctx) : defaultShow(el, message, ctx);
                    el.classList.remove("is-loading");
                }
            },
            clear: function (el, ctx) {
                el.classList.remove("is-loading");
                options.clear ? options.clear(el, ctx) : defaultClear(el, ctx);
            }
        });
    }

    /**
     * Trigger validation for the given element.
     */
    function validateField(el) {
        const elValidation = $(el).adaptTo("foundation-validation");
        elValidation.checkValidity();
        elValidation.updateUI();
    }

    window.BackendValidation = window.BackendValidation || {
        registerValidator: registerValidator,
        validateField: validateField
    };

}(window, Granite, Granite.$));