import assert from 'node:assert/strict';

import {
  buildContentVersions,
  normalizeNotesPayload,
  resolveChapterRequest,
} from '../src/utils/chapterDetailPayloads.mjs';

const versions = buildContentVersions([
  { userId: 12, username: '润色版' },
]);

assert.deepEqual(versions, [
  { userId: 0, username: '原版本' },
  { userId: 12, username: '润色版' },
]);

assert.deepEqual(buildContentVersions(''), [
  { userId: 0, username: '原版本' },
]);

assert.deepEqual(
  resolveChapterRequest(0, versions),
  { type: 'original', versionUserId: 0 }
);

assert.deepEqual(
  resolveChapterRequest(12, versions),
  { type: 'version', versionUserId: 12 }
);

assert.deepEqual(
  resolveChapterRequest(99, versions),
  { type: 'original', versionUserId: 0 }
);

assert.deepEqual(normalizeNotesPayload(''), []);
assert.deepEqual(normalizeNotesPayload(null), []);

const notes = [{ id: 1, content: 'note' }];
assert.equal(normalizeNotesPayload(notes), notes);

console.log('chapterDetailPayloads.spec.mjs passed');
