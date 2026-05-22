export function formatDefinitionLookupReason(word) {
  if (!word) return 'No lookup details recorded.';

  const parts = [];
  if (word.definitionLookupStatus) {
    parts.push(word.definitionLookupStatus.replaceAll('_', ' ').toLowerCase());
  }
  if (word.definitionLookupHttpStatus) {
    parts.push(`HTTP ${word.definitionLookupHttpStatus}`);
  }
  if (word.definitionLookupMessage) {
    parts.push(word.definitionLookupMessage);
  }

  return parts.length > 0 ? parts.join(' - ') : 'No lookup details recorded.';
}
