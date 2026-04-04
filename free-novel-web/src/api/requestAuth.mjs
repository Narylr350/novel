import CryptoJS from 'crypto-js';

function sortObject(obj) {
  if (typeof obj !== 'object' || obj === null) {
    return obj;
  }
  if (Array.isArray(obj)) {
    return obj.map(sortObject);
  }
  return Object.keys(obj)
    .sort()
    .reduce((acc, key) => {
      acc[key] = sortObject(obj[key]);
      return acc;
    }, {});
}

function generateSignature(config, secretKey) {
  const timestamp = Date.now();
  const nonce = CryptoJS.lib.WordArray.random(16).toString();

  let url = config.url;
  if (!url.startsWith('http')) {
    const baseURL = config.baseURL || window.location.origin;
    url = new URL(url, baseURL).href;
  }

  const method = config.method.toUpperCase();
  const path = new URL(url).pathname;
  const urlParams = {};
  if (config.url.includes('?')) {
    const queryStr = config.url.split('?')[1];
    const queryParams = new URLSearchParams(queryStr);
    for (const [key, value] of queryParams.entries()) {
      urlParams[key] = value;
    }
  }

  const stringifiedParams = config.params
    ? Object.entries(config.params).reduce((acc, [key, value]) => {
      acc[key] = String(value);
      return acc;
    }, {})
    : {};

  const signStr = [
    method,
    path,
    JSON.stringify(sortObject({ ...urlParams, ...stringifiedParams })),
    JSON.stringify(config.data ? sortObject(config.data) : {}),
    timestamp,
    nonce,
  ].join('|');

  return `;${CryptoJS.HmacSHA256(signStr, secretKey).toString()};${timestamp};${nonce}`;
}

export function buildAuthorizationHeader(config, token) {
  if (!token) {
    return null;
  }

  // The backend currently validates signatures with the credential token itself.
  return token + generateSignature(config, token);
}
