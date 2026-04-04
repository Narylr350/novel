import assert from 'node:assert/strict';

import {
  buildPlatformOptions,
  createAllTagSelection,
  resolveActiveTopic,
} from '../src/utils/platformOptions.mjs';

const options = buildPlatformOptions([
  { platformName: 'shutu' },
  { platformName: 'upload' },
]);

assert.deepEqual(options, [
  { label: 'shutu', value: 'shutu' },
  { label: 'upload', value: 'upload' },
]);

assert.equal(resolveActiveTopic(options, 'novelPia'), 'shutu');
assert.equal(resolveActiveTopic(options, 'upload'), 'upload');
assert.equal(resolveActiveTopic([], 'novelPia'), 'novelPia');

assert.deepEqual(createAllTagSelection('shutu'), [
  { id: 0, name: '全部', platform: 'shutu', trueName: 'all' },
]);

console.log('platformOptions.spec.mjs passed');
