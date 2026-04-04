import CryptoJS from 'crypto-js';

function buildCandidateTimestamps(now = Date.now()) {
  const currentMinute = Math.floor(now / 60000) * 60000;
  return [currentMinute, currentMinute - 60000];
}

function splitCiphertext(ciphertextBase64) {
  const combined = CryptoJS.enc.Base64.parse(ciphertextBase64);
  const ivSigBytes = 16;
  const cipherSigBytes = combined.sigBytes - ivSigBytes;

  if (cipherSigBytes <= 0) {
    throw new Error('ciphertext payload is shorter than iv');
  }

  const iv = CryptoJS.lib.WordArray.create(combined.words.slice(0, 4), ivSigBytes);
  const encrypted = CryptoJS.lib.WordArray.create(
    combined.words.slice(4, 4 + Math.ceil(cipherSigBytes / 4)),
    cipherSigBytes
  );

  return { iv, encrypted };
}

export function decryptChapterContent(ciphertextBase64, keyStr, now = Date.now()) {
  const { iv, encrypted } = splitCiphertext(ciphertextBase64);

  for (const timestamp of buildCandidateTimestamps(now)) {
    const key = CryptoJS.SHA256(`${keyStr}${timestamp}`);
    const decrypted = CryptoJS.AES.decrypt(
      { ciphertext: encrypted },
      key,
      { iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 }
    );

    const result = decrypted.toString(CryptoJS.enc.Utf8);
    if (result) {
      return result;
    }
  }

  throw new Error('解密过程错误: 所有密钥尝试失败');
}
