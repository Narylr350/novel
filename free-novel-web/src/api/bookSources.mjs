import service from './axios';

export function listBookSources() {
  return service.get('/api/book-sources').then((response) => response.data);
}

export function importBookSources(sourceJson) {
  return service.post('/api/book-sources/import', { sourceJson }).then((response) => response.data);
}

export function validateBookSources(sourceJson) {
  return service.post('/api/book-sources/validate', { sourceJson }).then((response) => response.data);
}

export function searchSourceBooks(payload) {
  return service.post('/api/book-sources/search', payload).then((response) => response.data);
}

export function getSourceBookDetail(payload) {
  return service.post('/api/book-sources/detail', payload).then((response) => response.data);
}

export function getSourceBookToc(payload) {
  return service.post('/api/book-sources/toc', payload).then((response) => response.data);
}

export function getSourceChapterContent(payload) {
  return service.post('/api/book-sources/content', payload).then((response) => response.data);
}
