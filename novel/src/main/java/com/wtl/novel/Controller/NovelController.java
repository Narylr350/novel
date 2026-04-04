package com.wtl.novel.Controller;

import com.wtl.novel.CDO.DiffDto;
import com.wtl.novel.CDO.NovelCTO;
import com.wtl.novel.CDO.NovelSearchRequest;
import com.wtl.novel.CDO.NovelWithStatusDTO;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.NovelService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.ChapterRepository;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.ReadingRecordRepository;
import com.wtl.novel.util.ObfuscateFontOTF;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/novels")
public class NovelController {

    @Autowired
    private NovelService novelService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObfuscateFontOTF obfuscateFontOTF;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private ReadingRecordRepository readingRecordRepository;
    @Autowired
    private NovelRepository novelRepository;



    private final Map<Long, ReentrantLock> novelLocks = new ConcurrentHashMap<>();

    @GetMapping("/repeatTrNovel/{novelId}")
    public String repeatTrNovel(@PathVariable Long novelId, HttpServletRequest httpServletRequest) {
        User userByToken = userService.getUserByToken(httpServletRequest);

        // 为每个novelId创建专用的锁对象
        ReentrantLock lock = novelLocks.computeIfAbsent(novelId, k -> new ReentrantLock());

        // 尝试获取锁，不等待
        if (!lock.tryLock()) {
            // 没拿到锁，直接返回
            return "系统正在处理该小说的翻译请求，请稍后再试";
        }

        try {
            return novelService.repeatTrNovel(novelId, userByToken.getId());
        } finally {
            lock.unlock();
            // 可选：清理长时间不用的锁对象
            if (!lock.hasQueuedThreads()) {
                novelLocks.remove(novelId, lock);
            }
        }
    }

    @GetMapping("/tag/{tagId}")
    public Page<Novel> getNovelsByTagId(@PathVariable Long tagId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return novelService.getNovelsByTagIdWithPagination(tagId, pageable);
    }

    @GetMapping("/deleteBook/{novelId}")
    public String deleteBook(@PathVariable Long novelId,HttpServletRequest httpServletRequest) {
        User userByToken = userService.getUserByToken(httpServletRequest);
        return novelService.deleteBook(novelId, userByToken.getId());
    }

    @GetMapping("/findByAuthorId/{authorName}")
    public List<NovelCTO> findByAuthorId(@PathVariable String authorName,HttpServletRequest request) {
        User userByToken = userService.getUserByToken(request);
        List<NovelCTO> novelsWithPagination = novelService.findByAuthorId(authorName,userByToken.getId());
        List<Long> novelIds = novelsWithPagination.stream().map(NovelCTO::getId).toList();
        Map<Long, Integer> longIntegerMap = chapterRepository.getChapterCountsByNovelIds(novelIds);
        Map<Long, List<String>> tagsForNovels = novelService.getTagsForNovels(novelIds);
        novelsWithPagination.forEach(novelCTO -> {
            novelCTO.setTags(tagsForNovels.getOrDefault(novelCTO.getId(), new ArrayList<>()));
            novelCTO.setChapterNum(longIntegerMap.getOrDefault(novelCTO.getId(), 0));
        });

        // 调用服务层方法
        return novelsWithPagination;
    }

    @GetMapping("/get")
    public Page<Novel> getNovelsWithPagination(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return novelService.getNovelsWithPagination(page, size);
    }
    @GetMapping("/executeRecommend")
    public Page<Novel> executeRecommend() {
        obfuscateFontOTF.process();
        novelService.executeRecommend();
        return null;
    }

    // 获取小说标签列表
    @GetMapping("/getTags/{novelId}")
    public List<Tag> getTagsByNovelId(@PathVariable Long novelId) {
        return novelService.getTagsByNovelId(novelId);
    }

    // 增加点赞数
    @PutMapping("/{id}/{type}/{favoriteType}/{groupId}")
    public ResponseEntity<Integer> increaseUp(@PathVariable Long id,
                                              @PathVariable String type,
                                              @PathVariable String favoriteType,
                                              @PathVariable Long groupId,
                                              HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(0);
        }
        return ResponseEntity.ok(novelService.increaseUp(id, credential.getUser(), type, favoriteType, groupId));
    }

    // 获取特定小说的信息
    @GetMapping("/{id}")
    public NovelCTO getNovelById(@PathVariable Long id, HttpServletRequest httpRequest) {
        return novelService.findNovelById(id, httpRequest);
    }

    @GetMapping("/updateTitleById/{title}/{id}")
    public boolean updateTitleById(@PathVariable String title,
                                    @PathVariable Long id) {
        return novelService.updateTitleById(title, id);
    }

    // 根据tag分页查询小说
//    @GetMapping("/page/{tagId}/{page}/{size}")
//    public Page<Novel> getNovels(@PathVariable Long tagId,@PathVariable int page, @PathVariable int size) {
//        return novelService.getNovelsWithPagination(tagId, page, size);
//    }

    @GetMapping("/searchByKeyWord")
    public Page<NovelWithStatusDTO> search(@RequestParam String keyword, HttpServletRequest httpRequest) {
        if (keyword != null && !keyword.isEmpty()) {
            String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
            String authorizationHeader = authorizationInfo[0];
            Credential credential = credentialService.findByToken(authorizationHeader);
            Page<NovelWithStatusDTO> nameContaining = novelService.findByTitleContainingOrTrueNameContaining(keyword, credential.getUser().getId());
            User user = credential.getUser();
            if (user.getHideReadBooks()) {
                List<ReadingRecord> readingRecords = readingRecordRepository.findByUserId(user.getId());

                Set<Long> readNovelIds = readingRecords.stream()
                        .map(ReadingRecord::getNovelId)
                        .collect(Collectors.toSet());
                List<NovelWithStatusDTO> filteredNovels = nameContaining.getContent().stream()
                        .filter(novel -> !readNovelIds.contains(novel.getId()))
                        .collect(Collectors.toList());
                Pageable pageable = PageRequest.of(0, 10);
                nameContaining = new PageImpl<>(
                        filteredNovels,
                        pageable,
                        nameContaining.getTotalElements());
            }

            List<Long> novelIds = nameContaining.getContent().stream().map(NovelWithStatusDTO::getId).toList();
            Map<Long, List<String>> tagsForNovels = novelService.getTagsForNovels(novelIds);
            nameContaining.getContent().forEach(novelCTO -> {
                novelCTO.setTags(tagsForNovels.getOrDefault(novelCTO.getId(), new ArrayList<>()));
            });
            return nameContaining;
        }
        return Page.empty();
    }

    @PostMapping("/addTag")
    public String addTag(
            @RequestBody DiffDto diffDto, HttpServletRequest httpRequest) throws Exception {
        User userByToken = userService.getUserByToken(httpRequest);
        return novelService.addTag(diffDto, userByToken);
    }



//    ====

//    // 根据tag分页查询小说
//    @GetMapping("/getNovelsByPlatform/{platform}/{tabId}/{fontNumber}/{page}/{size}")
//    public Page<Novel> getNovelsByPlatform(@PathVariable String platform,@PathVariable Long tabId,@PathVariable String fontNumber,@PathVariable int page, @PathVariable int size) {
//        return novelService.getNovelsWithPagination(platform, fontNumber,tabId,page, size);
//    }

    // 根据平台和标签分页查询小说
    @PostMapping("/getNovelsByPlatform")
    public Page<NovelCTO> getNovelsByPlatform(
            @RequestBody NovelSearchRequest request,HttpServletRequest httpRequest) throws Exception {
        Credential credential = resolveCredential(httpRequest);
        User user = credential != null ? credential.getUser() : null;
        String direction = request.getSort();
        Long userId = user != null ? user.getId() : null;
        String sort = request.getDirection();
        if (!((direction.equals("up") || direction.equals("novelLike")|| direction.equals("createdAt")|| direction.equals("updatedAt") || direction.equals("recommend") || direction.equals("novelRead")) && (sort.equals("asc") || sort.equals("desc")))) {
            System.out.println("userId:" + userId + "尝试入侵服务器的日志已记录");
            System.out.println("userId:" + userId + "尝试入侵服务器的日志已记录");
            System.out.println("userId:" + userId + "尝试入侵服务器的日志已记录");
            throw new Exception("userId:" + userId + "尝试入侵服务器的日志已记录");
        }
        // 创建分页请求
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),Sort.Direction.fromString(sort), direction);
        Page<NovelCTO> novelsWithPagination = novelService.getNovelsWithPagination(
                request.getPlatform(),
                request.getFontNumber(),
                request.getTabIds(),
                pageable, userId);


        if (user != null && Boolean.TRUE.equals(user.getHideReadBooks())) {
            List<ReadingRecord> readingRecords = readingRecordRepository.findByUserId(user.getId());

            Set<Long> readNovelIds = readingRecords.stream()
                    .map(ReadingRecord::getNovelId)
                    .collect(Collectors.toSet());
            List<NovelCTO> filteredNovels = novelsWithPagination.getContent().stream()
                    .filter(novel -> !readNovelIds.contains(novel.getId()))
                    .collect(Collectors.toList());

            novelsWithPagination = new PageImpl<>(
                    filteredNovels,
                    pageable,
                    novelsWithPagination.getTotalElements()
            );
        }
        List<Long> novelIds = novelsWithPagination.getContent().stream().map(NovelCTO::getId).toList();
        Map<Long, Integer> longIntegerMap = chapterRepository.getChapterCountsByNovelIds(novelIds);
        Map<Long, List<String>> tagsForNovels = novelService.getTagsForNovels(novelIds);
        novelsWithPagination.getContent().forEach(novelCTO -> {
            novelCTO.setTags(tagsForNovels.getOrDefault(novelCTO.getId(), new ArrayList<>()));
            novelCTO.setChapterNum(longIntegerMap.getOrDefault(novelCTO.getId(), 0));
        });

        // 调用服务层方法
        return novelsWithPagination;
    }

    private Credential resolveCredential(HttpServletRequest httpRequest) {
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        String[] authorizationInfo = authorization.split(";");
        if (authorizationInfo.length == 0 || authorizationInfo[0] == null || authorizationInfo[0].isBlank()) {
            return null;
        }

        // Reader-mode book browsing must keep working before login, so missing or expired
        // credentials downgrade to anonymous access instead of crashing the controller.
        Credential credential = credentialService.findByToken(authorizationInfo[0]);
        if (credential == null || credential.getExpiredAt() == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return credential;
    }

    /**
     * 导出小说为TXT文件下载
     */
    @GetMapping("/export/{novelId}")
    public void exportNovelToTxt(@PathVariable Long novelId, HttpServletResponse response) {
        try {
            // 获取小说信息
            Novel novel = novelRepository.findById(novelId).orElse(null);
            if (novel == null) {
                response.setContentType("text/plain; charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("小说不存在");
                return;
            }

            // 获取所有章节
            List<Chapter> chapters = chapterRepository.findByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(novelId);
            if (chapters.isEmpty()) {
                response.setContentType("text/plain; charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("该小说暂无章节");
                return;
            }

            // 处理文件名，移除特殊字符
            String safeTitle = novel.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_");
            String fileName = safeTitle + ".txt";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            
            // 设置响应头
            response.setContentType("text/plain; charset=UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
            response.setCharacterEncoding("UTF-8");

            // 写入文件内容
            try (OutputStream outputStream = response.getOutputStream()) {
                // 写入小说标题
                String header = "【" + novel.getTitle() + "】\n\n";
                outputStream.write(header.getBytes(StandardCharsets.UTF_8));

                // 写入每个章节
                for (Chapter chapter : chapters) {
                    StringBuilder chapterContent = new StringBuilder();
                    chapterContent.append("### 第").append(chapter.getChapterNumber()).append("章 : ")
                            .append(chapter.getTitle()).append(" ###\n");
                    String content = chapter.getContent();
                    if (content != null) {
                        chapterContent.append(content);
                    }
                    chapterContent.append("\n\n");
                    outputStream.write(chapterContent.toString().getBytes(StandardCharsets.UTF_8));
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            try {
                response.setContentType("text/plain; charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("导出失败: " + e.getMessage());
            } catch (IOException ex) {
                // 忽略
            }
        }
    }

}
