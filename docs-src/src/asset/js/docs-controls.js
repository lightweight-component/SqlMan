/* Controls shared by the English and Chinese documentation layouts. */
(function (window, document) {
    "use strict";

    function removeTrailingSlash(path) {
        return path.length > 1 ? path.replace(/\/+$/, "") : path;
    }

    function counterpartPath(currentLanguage) {
        var path = removeTrailingSlash(window.location.pathname);

        if (currentLanguage === "en")
            return path === "/docs" ? "/docs/cn" : path + "-cn";

        if (path === "/docs/cn")
            return "/docs";

        return path.replace(/-cn$/, "");
    }

    AJUtils.onReady(function () {
        var languageToggle = document.querySelector("[data-document-language]");
        var currentLanguage = languageToggle && languageToggle.getAttribute("data-document-language");

        AJUtils.initTheme();
        AJUtils.bindThemeToggle();

        if (languageToggle) {
            AJUtils.applyLanguage(currentLanguage, false);
            languageToggle.setAttribute("data-language-target", counterpartPath(currentLanguage));
            AJUtils.bindDocumentationLanguageToggle();
        }
    });
})(window, document);
