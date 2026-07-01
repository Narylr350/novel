import assert from 'node:assert/strict';

import { buildAuthorizationHeader } from '../src/api/requestAuth.mjs';
import { shouldResetAuthorizationForAuthRequest } from '../src/api/axios.js';

const signedHeader = buildAuthorizationHeader(
  {
    method: 'get',
    url: 'http://localhost:8081/api/novels/search?keyword=test',
    params: { page: 1 },
  },
  'reader-token'
);

assert.equal(buildAuthorizationHeader({ method: 'get', url: 'http://localhost:8081/api/novels/search' }, ''), null);
assert.equal(buildAuthorizationHeader({ method: 'get', url: 'http://localhost:8081/api/novels/search' }, null), null);
assert.equal(typeof signedHeader, 'string');
assert.equal(signedHeader.startsWith('reader-token;'), true);
assert.equal(shouldResetAuthorizationForAuthRequest('/api/auth/login'), true);
assert.equal(shouldResetAuthorizationForAuthRequest('/api/auth/logout'), true);
assert.equal(shouldResetAuthorizationForAuthRequest('/api/auth/isLogin'), false);
assert.equal(shouldResetAuthorizationForAuthRequest('/api/auth/app-mode'), false);

console.log('requestAuth.spec.mjs passed');
