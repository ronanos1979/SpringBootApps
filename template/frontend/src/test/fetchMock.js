export function jsonResponse(body, init = {}) {
  return {
    ok: init.ok ?? true,
    status: init.status ?? 200,
    statusText: init.statusText ?? 'OK',
    headers: {
      get: name => (name.toLowerCase() === 'content-type' ? 'application/json' : null),
    },
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(JSON.stringify(body)),
  };
}

export function emptyResponse(init = {}) {
  return {
    ok: init.ok ?? true,
    status: init.status ?? 200,
    statusText: init.statusText ?? 'OK',
    headers: {
      get: () => null,
    },
    json: () => Promise.resolve(null),
    text: () => Promise.resolve(''),
  };
}

export function mockFetchSequence(...responses) {
  const fetchMock = vi.spyOn(globalThis, 'fetch');
  responses.forEach(response => fetchMock.mockResolvedValueOnce(response));
  return fetchMock;
}
