import assert from 'node:assert/strict';

import {
  normalizeAppMode,
  isMaintainerMode,
  resolveAppModeRouteTarget,
} from '../src/config/appMode.mjs';

assert.equal(normalizeAppMode('reader'), 'reader');
assert.equal(normalizeAppMode('maintainer'), 'maintainer');
assert.equal(normalizeAppMode('maintainer '), 'maintainer');
assert.equal(normalizeAppMode('unexpected'), 'reader');

assert.equal(isMaintainerMode('maintainer'), true);
assert.equal(isMaintainerMode('reader'), false);

assert.deepEqual(
  resolveAppModeRouteTarget({ path: '/', meta: {} }, 'reader'),
  { name: 'WebLibrary' }
);
assert.deepEqual(
  resolveAppModeRouteTarget({ path: '/crawlerManager', meta: { appMode: 'maintainer' } }, 'reader'),
  { name: 'WebLibrary' }
);
assert.equal(
  resolveAppModeRouteTarget({ path: '/crawlerManager', meta: { appMode: 'maintainer' } }, 'maintainer'),
  null
);

console.log('appMode.spec.mjs passed');
