import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { decryptChapterContent } from '../src/utils/chapterCrypto.mjs';

function encryptLikeBackend(plaintext, keyStr, timestamp, ivHex) {
  const combinedKey = `${keyStr}${timestamp}`;
  const key = crypto.createHash('sha256').update(combinedKey, 'utf8').digest();
  const iv = Buffer.from(ivHex, 'hex');
  const cipher = crypto.createCipheriv('aes-256-cbc', key, iv);
  const encrypted = Buffer.concat([
    cipher.update(Buffer.from(plaintext, 'utf8')),
    cipher.final(),
  ]);

  return Buffer.concat([iv, encrypted]).toString('base64');
}

const fixedMinute = 1775278560000;
const plaintext = '第一段\n第二段';
const keyStr = 'reader-demo-token';
const ciphertext = encryptLikeBackend(
  plaintext,
  keyStr,
  fixedMinute,
  '00112233445566778899aabbccddeeff'
);

const originalDateNow = Date.now;
Date.now = () => fixedMinute + 15000;

try {
  const result = decryptChapterContent(ciphertext, keyStr);
  assert.equal(result, plaintext);
  console.log('signatureCrypto.spec.mjs passed');
} finally {
  Date.now = originalDateNow;
}
