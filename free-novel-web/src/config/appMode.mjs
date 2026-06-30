import service from '../api/axios.js';

export const ALLOWED_APP_MODES = ['reader', 'maintainer'];

let currentAppMode = 'reader';

export function normalizeAppMode(mode) {
  const normalizedMode = typeof mode === 'string' ? mode.trim() : mode;
  return ALLOWED_APP_MODES.includes(normalizedMode) ? normalizedMode : 'reader';
}

export function setAppMode(mode) {
  currentAppMode = normalizeAppMode(mode);
  return currentAppMode;
}

export function getAppMode() {
  return currentAppMode;
}

export function isMaintainerMode(mode = currentAppMode) {
  return normalizeAppMode(mode) === 'maintainer';
}

export function resolveAppModeRouteTarget(to, appMode = currentAppMode) {
  const normalizedMode = normalizeAppMode(appMode);
  if (normalizedMode !== 'reader') {
    return null;
  }

  if (to?.path === '/') {
    return { name: 'WebLibrary' };
  }

  if (to?.meta?.appMode === 'maintainer') {
    return { name: 'WebLibrary' };
  }

  return null;
}

export async function loadAppMode() {
  try {
    const response = await service.get('/api/auth/app-mode');
    return setAppMode(response?.data?.mode);
  } catch (error) {
    return setAppMode('reader');
  }
}
