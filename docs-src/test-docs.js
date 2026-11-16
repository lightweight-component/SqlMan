const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const output = path.join(__dirname, '.test-dist');
try {
    for (const language of ['en', 'zh']) {
        const index = JSON.parse(fs.readFileSync(path.join(output, 'search', language + '.json'), 'utf8'));
        assert.equal(index.version, 1);
        assert.ok(index.entries.length > 0);
        const pages = new Set();
        for (const entry of index.entries) {
            assert.equal(entry.language, language);
            assert.equal(entry.project, 'sqlman');
            const [url, anchor] = entry.url.split('#');
            assert.ok(url.startsWith('/docs/'), 'non-document URL in index: ' + url);
            assert.notEqual(url, '/docs/landing/');
            pages.add(url);
            const html = fs.readFileSync(path.join(output, url, 'index.html'), 'utf8');
            if (anchor)
                assert.ok(html.includes('id="' + decodeURIComponent(anchor) + '"'));
            assert.ok(html.includes('data-sqlman-search data-language="' + language + '"'));
            const shared = 'src="https://aj-util.ajaxjs.com/asset/js/static-search.js"';
            assert.ok(html.includes(shared));
            assert.ok(html.includes('href="https://aj-util.ajaxjs.com/asset/static-search.css"'));
            assert.ok(html.indexOf(shared) < html.indexOf('src="/asset/search.js"'));
        }
        const count = fs.readdirSync(path.join(__dirname, 'src/docs'), { recursive: true })
            .filter(file => file.endsWith('.md') && (language === 'zh' ? file === 'cn.md' || file.endsWith('-cn.md') : file !== 'cn.md' && !file.endsWith('-cn.md'))).length;
        assert.equal(pages.size, count, 'all source documentation pages must be indexed');
        console.log(language + ': ' + pages.size + ' documentation pages, ' + index.entries.length + ' sections verified.');
    }
    for (const route of ['', 'learn', 'docs/landing']) {
        const html = fs.readFileSync(path.join(output, route, 'index.html'), 'utf8');
        assert.ok(!html.includes('data-sqlman-search'), route + ': search must not be mounted outside documentation');
        assert.ok(!html.includes('static-search.js'), route + ': shared search must not load outside documentation');
    }
    console.log('Website homepage, interactive learning page and docs/landing marketing page are excluded.');
} finally {
    fs.rmSync(output, { recursive: true, force: true });
}
