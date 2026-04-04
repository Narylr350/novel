export function buildRequestErrorMessage(prefix, error) {
  const normalizedPrefix = typeof prefix === 'string' && prefix.trim().length > 0
    ? prefix.trim()
    : '请求失败';

  const responseData = error?.response?.data;
  if (typeof responseData === 'string' && responseData.trim().length > 0) {
    return `${normalizedPrefix}：${responseData.trim()}`;
  }

  const message = error?.message;
  if (typeof message === 'string' && message.trim().length > 0) {
    return `${normalizedPrefix}：${message.trim()}`;
  }

  return normalizedPrefix;
}
