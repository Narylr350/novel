import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

import { resolveAuthRouteTarget } from '../src/router/authGuard.mjs';

const __dirname = dirname(fileURLToPath(import.meta.url));
const routerSource = readFileSync(resolve(__dirname, '../src/router/index.js'), 'utf8');
const routeBlock = (routeName) => {
  const nameIndex = routerSource.indexOf(`name: '${routeName}'`);
  if (nameIndex < 0) {
    return '';
  }
  const start = routerSource.lastIndexOf('    {', nameIndex);
  const end = routerSource.indexOf('    }', nameIndex);
  return start >= 0 && end >= 0 ? routerSource.slice(start, end + 5) : '';
};

assert.equal(
  resolveAuthRouteTarget(
    { name: 'NovelDetail', fullPath: '/novelDetail/123', meta: { publicAccess: true } },
    null
  ),
  null
);

assert.equal(
  resolveAuthRouteTarget(
    { name: 'ChapterDetail', fullPath: '/chapterDetail/456', meta: { publicAccess: true } },
    'undefined'
  ),
  null
);

assert.deepEqual(
  resolveAuthRouteTarget(
    { name: 'WebFavorites', fullPath: '/favorites', meta: {} },
    null
  ),
  { name: 'WebLogin', query: { redirect: '/favorites' } }
);

assert.equal(
  resolveAuthRouteTarget(
    { name: 'WebFavorites', fullPath: '/favorites', meta: {} },
    'reader-token'
  ),
  null
);

assert.equal(
  resolveAuthRouteTarget(
    { name: 'WebLogin', fullPath: '/login', meta: {} },
    null
  ),
  null
);

assert.match(routerSource, /name: 'ChapterDetail',[\s\S]*?meta: \{[^}]*publicAccess: true/);
assert.match(routerSource, /name: 'NovelDetail',[\s\S]*?meta: \{[^}]*publicAccess: true/);
assert.match(routerSource, /resolveAuthRouteTarget\(to, token\)/);

for (const routeName of ['SourceSearch', 'SourceBookDetail', 'SourceChapterDetail']) {
  assert.match(routerSource, new RegExp(`name: '${routeName}'`));
  assert.ok(routeBlock(routeName).length > 0, `${routeName} route should be registered`);
  assert.doesNotMatch(routeBlock(routeName), /publicAccess: true/);
  assert.deepEqual(
    resolveAuthRouteTarget(
      { name: routeName, fullPath: `/source/test/${routeName}`, meta: {} },
      null
    ),
    { name: 'WebLogin', query: { redirect: `/source/test/${routeName}` } }
  );
}

console.log('routeAuth.spec.mjs passed');
