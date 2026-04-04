from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import os
import urllib.parse

MODE = os.environ.get('APP_UI_MODE', 'reader')

class Handler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        return

    def _write_json(self, payload, status=200):
        body = json.dumps(payload, ensure_ascii=False).encode('utf-8')
        self.send_response(status)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Headers', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Headers', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.end_headers()

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path
        if path == '/api/auth/app-mode':
            return self._write_json({'mode': MODE})
        if path == '/api/auth/isLogin':
            return self._write_json(True)
        if path == '/api/msg/getMessage':
            return self._write_json({'totalElements': 0, 'content': []})
        if path == '/api/user/getUserDetail':
            return self._write_json({'email': 'reader@example.com', 'point': 12, 'hideReadBooks': False})
        if path in ('/api/user/getCode', '/api/user/geneCode'):
            return self._write_json([])
        if path == '/api/novels/1':
            return self._write_json({
                'id': 1,
                'title': '测试小说',
                'photoUrl': '',
                'up': 10,
                'recommend': 5,
                'fontNumber': 12345,
                'novelRead': 20,
                'novelLike': 3,
                'favoriteGroup': '默认',
                'authorName': '测试作者',
                'lastChapter': 0,
                'lastChapterId': 101,
                'platform': 'novelPia',
                'spans': '测试标签'
            })
        if path == '/api/chapters/getChaptersByNovelId/1':
            return self._write_json([
                {'id': 101, 'chapterNumber': 1, 'title': '第一章', 'ownPhoto': False},
                {'id': 102, 'chapterNumber': 2, 'title': '第二章', 'ownPhoto': True}
            ])
        if path == '/api/chaptersExecute/novel/1':
            return self._write_json([
                {'id': 201, 'chapterNumber': 3, 'title': '待汉化章节'}
            ])
        if path.startswith('/api/favorites/user/1/'):
            return self._write_json(False)
        if path == '/api/tag/getTagsAllInfoByNovelId/1':
            return self._write_json([{'id': 1, 'name': '测试'}])
        if path == '/api/posts/getAllPostsByNovelId':
            return self._write_json({'content': [{'id': 301, 'title': '评论标题', 'content': '评论内容', 'author': '读者', 'createdAt': '2026-04-03T00:00:00'}], 'totalElements': 1})
        if path == '/api/posts/getPostsByUserId':
            return self._write_json({'content': [], 'last': True})
        if path == '/api/dic/findCookieByUserId':
            return self._write_json([])
        if path == '/api/platform/novel':
            return self._write_json([])
        if path == '/api/dic/getHome':
            return self._write_json([])
        return self._write_json({'path': path}, 404)

HTTPServer(('127.0.0.1', 8081), Handler).serve_forever()
