---
title: SqlMan Documentation Sources
description: Build and development notes for the Eleventy-based SqlMan documentation site.
tags:
  - SqlMan
  - documentation
  - Eleventy
---

Powered by [Eleventy](https://www.11ty.dev/) +

This is a Node.js project. Run `npm install` to install dependencies and `npm run dev` to start the development server.

Easy to write HTML content in this environment, just pure HTML/CSS/JS is enough, plus Markdown to HTML is supported.

## Documentation-only search

Only the documentation layouts load `https://aj-util.ajaxjs.com/asset/js/static-search.js` and `https://aj-util.ajaxjs.com/asset/static-search.css`, followed by the local `/asset/search.js` adapter. `/asset/search.css` supplies theme/mobile integration only; the reusable component is not copied into this repository.

The index boundary is intentionally strict: `lib/search-index.js` requires both a Markdown source under `src/docs/` and a public URL under `/docs/`, plus the documentation article markup. The website homepage, blog/marketing pages, `/learn/`, and even the marketing HTML page `src/docs/landing.html` are excluded. Those non-document pages do not mount the search component. Their navigation and footer text are not part of the documentation index either.

`eleventy.config.js` generates version-1 `dist/search/en.json` and `dist/search/zh.json` from rendered sections and adds missing heading IDs. Indexes belong to the SqlMan site, not the shared component host. Each indexed result must link to a real document heading; update source Markdown rather than generated JSON.

Run from `docs-src`:

```sh
npm test
npm run build
```

The tests cover source/URL exclusion rules, both languages, duplicate/preserved heading anchors, all document pages and shared resource load order. They also assert that the homepage, interactive learning page and `docs/landing` do not load or mount search. Test builds use `.test-dist` and remove it afterward.

Optional real-browser verification after building: `node test-search-browser.js`. It uses the real online shared resources and local indexes, checks Chinese/English search, highlights, keyboard/section navigation, mobile/dark mode and failed-index retry, and verifies that website pages have no search widget. Playwright and a browser are required; optional `AJ_DOCS_PLAYWRIGHT` and `AJ_DOCS_BROWSER` override their locations. Network access to shared resources and the website's existing dependencies is needed.

Deploy documentation pages, the adapter/styles and `search/` indexes consistently; a local build is not a publication. Existing website pages remain outside the search scope. Use HTTP/HTTPS rather than `file://`.

## Less.js support

Install https://www.npmjs.com/package/eleventy-plugin-less
