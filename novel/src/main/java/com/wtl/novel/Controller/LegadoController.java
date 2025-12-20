package com.wtl.novel.Controller;

import com.wtl.novel.CDO.NovelCTO;
import com.wtl.novel.DTO.ChapterProjection;
import com.wtl.novel.Service.ChapterService;
import com.wtl.novel.Service.NovelService;
import com.wtl.novel.entity.Chapter;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.repository.ChapterRepository;
import com.wtl.novel.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阅读APP书源专用接口
 * 提供公开访问的API，无需认证
 */
@RestController
@RequestMapping("/api/legado")
public class LegadoController {

    /**
     * 获取基础URL
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        if (("http".equals(scheme) && serverPort == 80) || ("https".equals(scheme) && serverPort == 443)) {
            return scheme + "://" + serverName;
        }
        return scheme + "://" + serverName + ":" + serverPort;
    }

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private NovelService novelService;

    @Autowired
    private ChapterService chapterService;

    /**
     * 搜索小说
     * @param keyword 搜索关键词
     * @return 小说列表
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(defaultValue = "") String keyword, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String baseUrl = getBaseUrl(request);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            result.put("books", new ArrayList<>());
            return result;
        }
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Novel> novels = novelRepository
                .findByTitleContainingAndIsDeletedFalseOrTrueNameContainingAndIsDeletedFalseOrderByUpDesc(
                        keyword.trim(), keyword.trim(), pageable);

        List<Map<String, Object>> bookList = new ArrayList<>();
        for (Novel novel : novels.getContent()) {
            Map<String, Object> book = new HashMap<>();
            book.put("id", novel.getId());
            book.put("name", novel.getTitle());
            book.put("author", novel.getAuthorName() != null ? novel.getAuthorName() : "未知");
            book.put("coverUrl", novel.getPhotoUrl());
            book.put("intro", novel.getSpans() != null ? novel.getSpans() : "");
            book.put("kind", novel.getNovelType() != null ? novel.getNovelType() : "");
            book.put("wordCount", novel.getFontNumber() != null ? novel.getFontNumber() : 0);
            book.put("bookUrl", baseUrl + "/api/legado/book/" + novel.getId());
            bookList.add(book);
        }
        result.put("books", bookList);
        return result;
    }

    /**
     * 获取小说详情
     * @param novelId 小说ID
     * @return 小说详情
     */
    @GetMapping("/book/{novelId}")
    public Map<String, Object> getBookInfo(@PathVariable Long novelId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String baseUrl = getBaseUrl(request);
        
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            result.put("error", "小说不存在");
            return result;
        }

        result.put("id", novel.getId());
        result.put("name", novel.getTitle());
        result.put("author", novel.getAuthorName() != null ? novel.getAuthorName() : "未知");
        result.put("coverUrl", novel.getPhotoUrl());
        result.put("intro", novel.getSpans() != null ? novel.getSpans() : "");
        result.put("kind", novel.getNovelType() != null ? novel.getNovelType() : "");
        result.put("wordCount", novel.getFontNumber() != null ? novel.getFontNumber() : 0);
        result.put("tocUrl", baseUrl + "/api/legado/toc/" + novel.getId());

        // 获取最新章节
        List<ChapterProjection> chapters = chapterService.getChaptersByNovelId(novelId);
        if (!chapters.isEmpty()) {
            ChapterProjection lastChapter = chapters.get(chapters.size() - 1);
            result.put("lastChapter", lastChapter.getTitle());
        }

        // 获取标签
        List<String> tags = novelService.getTagsForNovels(List.of(novelId)).getOrDefault(novelId, new ArrayList<>());
        result.put("tags", tags);

        return result;
    }

    /**
     * 获取小说目录
     * @param novelId 小说ID
     * @return 章节列表
     */
    @GetMapping("/toc/{novelId}")
    public List<Map<String, Object>> getToc(@PathVariable Long novelId, HttpServletRequest request) {
        List<Map<String, Object>> result = new ArrayList<>();
        String baseUrl = getBaseUrl(request);
        List<ChapterProjection> chapters = chapterService.getChaptersByNovelId(novelId);

        for (ChapterProjection chapter : chapters) {
            Map<String, Object> chapterInfo = new HashMap<>();
            chapterInfo.put("chapterName", chapter.getTitle());
            chapterInfo.put("chapterUrl", baseUrl + "/api/legado/content/" + chapter.getId());
            chapterInfo.put("chapterNumber", chapter.getChapterNumber());
            result.add(chapterInfo);
        }
        return result;
    }

    /**
     * 获取章节内容
     * @param chapterId 章节ID
     * @return 章节内容
     */
    @GetMapping("/content/{chapterId}")
    public Map<String, Object> getContent(@PathVariable Long chapterId) {
        Map<String, Object> result = new HashMap<>();
        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter == null || chapter.isDeleted()) {
            result.put("error", "章节不存在");
            return result;
        }

        result.put("title", chapter.getTitle());
        result.put("content", chapter.getContent());
        result.put("chapterNumber", chapter.getChapterNumber());
        result.put("novelId", chapter.getNovelId());

        return result;
    }

    /**
     * 发现页面 - 获取推荐小说
     * @param page 页码
     * @param size 每页数量
     * @return 小说列表
     */
    @GetMapping("/explore")
    public Map<String, Object> explore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String baseUrl = getBaseUrl(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<Novel> novels = novelRepository.findByIsDeletedFalseOrderByUpDesc(pageable);

        List<Map<String, Object>> bookList = new ArrayList<>();
        for (Novel novel : novels.getContent()) {
            Map<String, Object> book = new HashMap<>();
            book.put("id", novel.getId());
            book.put("name", novel.getTitle());
            book.put("author", novel.getAuthorName() != null ? novel.getAuthorName() : "未知");
            book.put("coverUrl", novel.getPhotoUrl());
            book.put("intro", novel.getSpans() != null ? novel.getSpans() : "");
            book.put("kind", novel.getNovelType() != null ? novel.getNovelType() : "");
            book.put("wordCount", novel.getFontNumber() != null ? novel.getFontNumber() : 0);
            book.put("bookUrl", baseUrl + "/api/legado/book/" + novel.getId());
            bookList.add(book);
        }
        result.put("books", bookList);
        result.put("totalPages", novels.getTotalPages());
        result.put("totalElements", novels.getTotalElements());
        return result;
    }

    /**
     * 按平台获取小说
     * @param platform 平台名称 (novelPia, syosetu, upload等)
     * @param page 页码
     * @param size 每页数量
     * @return 小说列表
     */
    @GetMapping("/explore/{platform}")
    public Map<String, Object> exploreByPlatform(
            @PathVariable String platform,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String baseUrl = getBaseUrl(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<Novel> novels = novelRepository.findByPlatformAndIsDeletedFalseOrderByUpDesc(platform, pageable);

        List<Map<String, Object>> bookList = new ArrayList<>();
        for (Novel novel : novels.getContent()) {
            Map<String, Object> book = new HashMap<>();
            book.put("id", novel.getId());
            book.put("name", novel.getTitle());
            book.put("author", novel.getAuthorName() != null ? novel.getAuthorName() : "未知");
            book.put("coverUrl", novel.getPhotoUrl());
            book.put("intro", novel.getSpans() != null ? novel.getSpans() : "");
            book.put("kind", novel.getNovelType() != null ? novel.getNovelType() : "");
            book.put("wordCount", novel.getFontNumber() != null ? novel.getFontNumber() : 0);
            book.put("bookUrl", baseUrl + "/api/legado/book/" + novel.getId());
            bookList.add(book);
        }
        result.put("books", bookList);
        result.put("totalPages", novels.getTotalPages());
        result.put("totalElements", novels.getTotalElements());
        return result;
    }
}
