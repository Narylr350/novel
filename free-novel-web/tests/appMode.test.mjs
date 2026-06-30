import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import test from 'node:test';
import assert from 'node:assert/strict';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const appModeSource = readFileSync(resolve(__dirname, '../src/config/appMode.mjs'), 'utf8');

test('loadAppMode uses the shared API client instead of raw axios', () => {
  assert.match(appModeSource, /import\s+service\s+from\s+['"]\.\.\/api\/axios\.js['"]/);
  assert.doesNotMatch(appModeSource, /import\s+axios\s+from\s+['"]axios['"]/);
  assert.match(appModeSource, /service\.get\(['"]\/api\/auth\/app-mode['"]\)/);
});
