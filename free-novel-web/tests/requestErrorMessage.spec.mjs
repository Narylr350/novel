import assert from 'node:assert/strict';

import { buildRequestErrorMessage } from '../src/utils/requestErrorMessage.mjs';

assert.equal(buildRequestErrorMessage('获取平台错误', null), '获取平台错误');
assert.equal(
  buildRequestErrorMessage('获取平台错误', { message: 'Network Error' }),
  '获取平台错误：Network Error'
);
assert.equal(
  buildRequestErrorMessage('获取平台错误', { response: { data: '缺少认证信息' } }),
  '获取平台错误：缺少认证信息'
);

console.log('requestErrorMessage.spec.mjs passed');
