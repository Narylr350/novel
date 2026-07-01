import assert from 'node:assert/strict';

import { splitSourceContentLines } from '../src/utils/sourceContentLines.mjs';

assert.deepEqual(splitSourceContentLines('第一段\n第二段'), ['第一段', '第二段']);
assert.deepEqual(splitSourceContentLines(''), []);

const longOneLine = '这是一个很长的书源正文。'.repeat(40);
const lines = splitSourceContentLines(longOneLine);

assert.ok(lines.length > 1, 'long one-line content should be split into readable paragraphs');
assert.ok(lines.every((line) => line.length <= 220), 'fallback paragraphs should stay readable');

console.log('sourceContentLines.spec.mjs passed');
