export function buildContentVersions(payload) {
  const versions = Array.isArray(payload) ? payload : [];

  return [
    { userId: 0, username: '原版本' },
    ...versions,
  ];
}

export function resolveChapterRequest(selectedVersionUserId, allContentVersion) {
  const normalizedVersionId = Number(selectedVersionUserId) || 0;
  const hasSelectedVersion = Array.isArray(allContentVersion)
    && allContentVersion.some((item) => item.userId === normalizedVersionId);

  if (normalizedVersionId > 0 && hasSelectedVersion) {
    return { type: 'version', versionUserId: normalizedVersionId };
  }

  return { type: 'original', versionUserId: 0 };
}

export function normalizeNotesPayload(payload) {
  return Array.isArray(payload) ? payload : [];
}
