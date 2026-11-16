const assert = require('node:assert/strict');
const { indexPage } = require('./lib/search-index');
const html = '<html lang="zh-CN"><nav>noise</nav><article class="aj-text"><h1>查询</h1><p>JDBC SQL</p>'
    + '<h2>分页</h2><p>PageResult</p><h2>分页</h2><p>下一页</p><h3 id="existing">已有锚点</h3></article></html>';
const result = indexPage(html, '/docs/query/query-paging-cn/', './src/docs/query/query-paging-cn.md');
assert.equal(result.entries.length, 4);
assert.ok(result.entries.every(entry => entry.project === 'sqlman' && entry.language === 'zh'));
assert.ok(result.html.includes('id="search-分页-2"'));
assert.ok(result.entries.some(entry => entry.url.endsWith('#existing')));
assert.ok(!result.entries.some(entry => entry.text.includes('noise')));
// Source path AND document URL are required, even if a marketing page has identical article markup.
for (const [url, source] of [
    ['/', './src/index.njk'], ['/learn/', './src/learn.njk'], ['/blog/query/', './src/blog/query.md'],
    ['/docs/landing/', './src/docs/landing.html'], ['/docs/fake/', './src/marketing/fake.md'],
    ['/outside/', './src/docs/page.md'], ['/docstring/', './src/docs/page.md']
]) {
    assert.deepEqual(indexPage(html, url, source).entries, [], url + ' must be excluded');
}
assert.equal(indexPage(html.replace('zh-CN', 'en'), '/docs/', 'src\\docs\\index.md').entries[0].language, 'en');
console.log('SQLMan document-only source/URL boundary, languages and preserved/duplicate anchors verified.');
