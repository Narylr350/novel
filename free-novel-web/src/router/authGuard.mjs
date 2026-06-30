const LOGIN_ROUTE_NAME = 'WebLogin';

export function isAuthenticated(token) {
  return Boolean(token && token !== 'undefined');
}

export function resolveAuthRouteTarget(to, token) {
  if (isAuthenticated(token) || to?.name === LOGIN_ROUTE_NAME || to?.meta?.publicAccess) {
    return null;
  }

  return { name: LOGIN_ROUTE_NAME, query: { redirect: to.fullPath } };
}
