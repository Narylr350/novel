export function buildPlatformOptions(platforms) {
  return (platforms ?? [])
    .map((item) => item?.platformName)
    .filter((value, index, array) => value && array.indexOf(value) === index)
    .map((platformName) => ({
      label: platformName,
      value: platformName,
    }));
}

export function resolveActiveTopic(options, currentTopic) {
  if (!options?.length) {
    return currentTopic;
  }

  const values = options.map((option) => option.value);
  return values.includes(currentTopic) ? currentTopic : values[0];
}

export function createAllTagSelection(platform) {
  return [{ id: 0, name: '全部', platform, trueName: 'all' }];
}
