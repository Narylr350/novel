package com.wtl.novel.Controller;

import com.wtl.novel.Config.TaskSchedulerManager;
import com.wtl.novel.entity.ChapterExecute;
import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.repository.ChapterExecuteRepository;
import com.wtl.novel.repository.ChapterRepository;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.translator.Novelpia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬虫管理Controller
 * 提供爬虫功能的独立管理接口，可以关闭AI翻译只使用爬虫
 */
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    private static final Logger log = LoggerFactory.getLogger(CrawlerController.class);

    @Autowired
    private Novelpia novelpia;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private DictionaryRepository dictionaryRepository;


    @Value("${task.scheduler.enabled:false}")
    private boolean schedulerEnabled;

    @Value("${task.novelpia.task2.enabled:false}")
    private boolean task2Enabled;

    @Value("${task.novelpia.task3.enabled:false}")
    private boolean task3Enabled;

    // ==================== 爬虫状态 ====================

    /**
     * 获取爬虫系统状态概览
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCrawlerStatus() {
        Map<String, Object> status = new HashMap<>();

        // 统计信息
        long totalNovels = novelRepository.count();
        long pendingChapters = chapterExecuteRepository.countByNowStateAndIsDeletedFalse(0);
        long translatingChapters = chapterExecuteRepository.countByNowStateAndIsDeletedFalse(3);
        long errorChapters = chapterExecuteRepository.countByNowStateAndIsDeletedFalse(1);
        long completedChapters = chapterRepository.count();

        status.put("totalNovels", totalNovels);
        status.put("pendingChapters", pendingChapters);
        status.put("translatingChapters", translatingChapters);
        status.put("errorChapters", errorChapters);
        status.put("completedChapters", completedChapters);

        // 任务开关状态
        status.put("schedulerEnabled", schedulerEnabled);
        status.put("translationEnabled", task2Enabled);
        status.put("downloadEnabled", task3Enabled);

        // 从数据库读取配置开关
        try {
            Dictionary executeDownload = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeDownload");
            Dictionary executeTr = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeTr");
            
            status.put("dbDownloadEnabled", executeDownload != null && "true".equals(executeDownload.getValueField()));
            status.put("dbTranslationEnabled", executeTr != null && "true".equals(executeTr.getValueField()));
        } catch (Exception e) {
            status.put("dbDownloadEnabled", false);
            status.put("dbTranslationEnabled", false);
        }

        return ResponseEntity.ok(status);
    }

    // ==================== 配置管理 ====================

    /**
     * 获取所有爬虫相关配置
     */
    @GetMapping("/config")
    public ResponseEntity<List<Map<String, Object>>> getCrawlerConfig() {
        // 获取爬虫相关的配置项
        String[] configKeys = {
            "executeDownload",      // 下载开关
            "executeTr",            // 翻译开关
            "proxyClient",          // 代理开关
            "upNumber",             // 收藏数阈值
            "novelPiaRegex",        // 正则表达式
            "novelPiaDetail",       // 详情页URL
            "novelPiaSearchUrl",    // 搜索URL
            "novelPiaGetNovelChapter", // 章节获取URL
            "novelPiaGetPage",      // 分页URL
            "shutuTr"               // 书途翻译开关
        };

        List<Map<String, Object>> configs = new java.util.ArrayList<>();
        for (String key : configKeys) {
            try {
                Dictionary dict = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(key);
                if (dict != null) {
                    Map<String, Object> config = new HashMap<>();
                    config.put("id", dict.getId());
                    config.put("key", dict.getKeyField());
                    config.put("value", dict.getValueField());
                    config.put("description", dict.getDescription());
                    configs.add(config);
                }
            } catch (Exception e) {
                log.warn("读取配置失败: {}", key);
            }
        }

        return ResponseEntity.ok(configs);
    }

    /**
     * 更新配置
     */
    @PutMapping("/config/{key}")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> result = new HashMap<>();
        String value = request.get("value");

        try {
            Dictionary dict = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(key);
            if (dict == null) {
                result.put("success", false);
                result.put("message", "配置项不存在");
                return ResponseEntity.badRequest().body(result);
            }

            dict.setValueField(value);
            dictionaryRepository.save(dict);

            result.put("success", true);
            result.put("message", "配置已更新");
            result.put("key", key);
            result.put("value", value);

            log.info("配置已更新: {} = {}", key, value);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // ==================== 快捷开关 ====================

    /**
     * 关闭AI翻译功能
     */
    @PostMapping("/disable-translation")
    public ResponseEntity<Map<String, Object>> disableTranslation() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 关闭翻译相关开关
            updateDictionaryValue("executeTr", "false");
            updateDictionaryValue("executeSyosetuTr", "false");
            updateDictionaryValue("shutuTr", "false");
            updateDictionaryValue("executeNovelPiaTrError", "false");
            updateDictionaryValue("executeSyosetuTrError", "false");

            result.put("success", true);
            result.put("message", "AI翻译功能已关闭");
            log.info("AI翻译功能已关闭");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 启用AI翻译功能
     */
    @PostMapping("/enable-translation")
    public ResponseEntity<Map<String, Object>> enableTranslation() {
        Map<String, Object> result = new HashMap<>();
        try {
            updateDictionaryValue("executeTr", "true");
            
            result.put("success", true);
            result.put("message", "AI翻译功能已启用");
            log.info("AI翻译功能已启用");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 启用爬虫下载功能
     */
    @PostMapping("/enable-download")
    public ResponseEntity<Map<String, Object>> enableDownload() {
        Map<String, Object> result = new HashMap<>();
        try {
            updateDictionaryValue("executeDownload", "true");
            
            result.put("success", true);
            result.put("message", "爬虫下载功能已启用");
            log.info("爬虫下载功能已启用");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 关闭爬虫下载功能
     */
    @PostMapping("/disable-download")
    public ResponseEntity<Map<String, Object>> disableDownload() {
        Map<String, Object> result = new HashMap<>();
        try {
            updateDictionaryValue("executeDownload", "false");
            
            result.put("success", true);
            result.put("message", "爬虫下载功能已关闭");
            log.info("爬虫下载功能已关闭");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // ==================== 手动触发爬虫 ====================

    /**
     * 手动触发下载任务
     */
    @PostMapping("/trigger-download")
    public ResponseEntity<Map<String, Object>> triggerDownload() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("手动触发下载任务");
            // 异步执行下载任务
            new Thread(() -> {
                try {
                    novelpia.executeDownload();
                } catch (Exception e) {
                    log.error("下载任务执行失败", e);
                }
            }).start();

            result.put("success", true);
            result.put("message", "下载任务已触发，正在后台执行");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "触发失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 下载指定小说的章节
     */
    @PostMapping("/download-novel/{novelId}")
    public ResponseEntity<Map<String, Object>> downloadNovel(@PathVariable Long novelId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Novel novel = novelRepository.findById(novelId).orElse(null);
            if (novel == null) {
                result.put("success", false);
                result.put("message", "小说不存在");
                return ResponseEntity.badRequest().body(result);
            }

            log.info("手动下载小说: {} (ID: {})", novel.getTitle(), novelId);
            
            // 异步执行
            new Thread(() -> {
                try {
                    novelpia.executeDownloadOne(novelId);
                } catch (Exception e) {
                    log.error("下载小说失败: {}", novelId, e);
                }
            }).start();

            result.put("success", true);
            result.put("message", "已开始下载: " + novel.getTitle());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // ==================== 待处理章节查询 ====================

    /**
     * 获取待下载的小说列表
     */
    @GetMapping("/pending-novels")
    public ResponseEntity<List<Map<String, Object>>> getPendingNovels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // 获取有待处理章节的小说
        List<Object[]> novels = chapterExecuteRepository.findNovelsWithPendingChapters();
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        int start = page * size;
        int end = Math.min(start + size, novels.size());
        
        for (int i = start; i < end; i++) {
            Object[] row = novels.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("novelId", row[0]);
            item.put("pendingCount", row[1]);
            
            // 获取小说信息
            Novel novel = novelRepository.findById((Long) row[0]).orElse(null);
            if (novel != null) {
                item.put("title", novel.getTitle());
                item.put("platform", novel.getPlatform());
            }
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== 辅助方法 ====================

    private void updateDictionaryValue(String key, String value) {
        Dictionary dict = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(key);
        if (dict != null) {
            dict.setValueField(value);
            dictionaryRepository.save(dict);
        } else {
            // 如果不存在则创建新配置
            Dictionary newDict = new Dictionary();
            newDict.setKeyField(key);
            newDict.setValueField(value);
            newDict.setIsDeleted(false);
            dictionaryRepository.save(newDict);
        }
    }

    // ==================== 定时任务开关管理 ====================

    /**
     * 获取所有定时任务开关状态
     */
    @GetMapping("/task-switches")
    public ResponseEntity<List<Map<String, Object>>> getTaskSwitches() {
        String[][] taskConfigs = {
            {"task.scheduler", "总调度开关", "控制所有定时任务的执行"},
            {"task.novelpia.task2", "翻译任务", "执行章节翻译"},
            {"task.novelpia.task3", "下载任务", "执行章节下载"},
            {"task.novelpia.photo", "图片任务", "处理小说图片"},
            {"task.novelpia.fix.error.chapter", "错误修复", "修复下载失败的章节"},
            {"task.novelpia.crawler", "爬虫任务", "爬取新小说信息"},
            {"task.sitemap.html.generator", "HTML生成", "生成小说HTML页面"},
            {"task.sitemap.index.generator", "索引生成", "生成网站索引"},
            {"task.update.novel.from.file", "文件更新", "从文件更新小说数据"},
            {"task.chapter.sync", "章节同步", "同步章节到从库"}
        };

        List<Map<String, Object>> switches = new java.util.ArrayList<>();
        for (String[] config : taskConfigs) {
            Map<String, Object> item = new HashMap<>();
            item.put("key", config[0]);
            item.put("name", config[1]);
            item.put("description", config[2]);
            
            // 从数据库读取开关状态
            try {
                Dictionary dict = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(config[0]);
                item.put("enabled", dict != null && "true".equalsIgnoreCase(dict.getValueField()));
            } catch (Exception e) {
                item.put("enabled", false);
            }
            switches.add(item);
        }

        return ResponseEntity.ok(switches);
    }

    /**
     * 切换定时任务开关
     */
    @PostMapping("/task-switch/{key}")
    public ResponseEntity<Map<String, Object>> toggleTaskSwitch(
            @PathVariable String key,
            @RequestBody Map<String, Boolean> request) {
        
        Map<String, Object> result = new HashMap<>();
        Boolean enabled = request.get("enabled");

        if (enabled == null) {
            result.put("success", false);
            result.put("message", "参数错误");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            updateDictionaryValue(key, enabled.toString());
            
            result.put("success", true);
            result.put("key", key);
            result.put("enabled", enabled);
            result.put("message", enabled ? "任务已启用" : "任务已关闭");
            
            log.info("任务开关已更新: {} = {}", key, enabled);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

}
