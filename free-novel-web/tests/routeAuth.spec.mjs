import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

import { resolveAuthRouteTarget } from '../src/router/authGuard.mjs';

const __dirname = dirname(fileURLToPath(import.meta.url));
const routerSource = readFileSync(resolve(__dirname, '../src/router/index.js'), 'utf8');

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

console.log('routeAuth.spec.mjs passed');
