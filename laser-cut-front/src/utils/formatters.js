export function formatDimensionValue(value) {
  if (value === null || value === undefined) {
    return '--';
  }
  const numericValue = Number(value);
  if (Number.isNaN(numericValue)) {
    return '--';
  }
  return numericValue.toFixed(3).replace(/\.?0+$/, '');
}

export function formatDimensionValueForSummary(value) {
  if (value === null || value === undefined) {
    return null;
  }
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return null;
  }
  return numeric.toFixed(3).replace(/\.?0+$/, '');
}
