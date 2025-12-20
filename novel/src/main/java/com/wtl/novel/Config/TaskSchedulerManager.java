package com.wtl.novel.Config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.siteMap.NovelHtmlGenerator;
import com.wtl.novel.siteMap.NovelIndexGenerator;
import com.wtl.novel.translator.Novelpia;
import com.wtl.novel.util.NovelpiaCrawler;
import com.wtl.novel.util.UpdateNovelFromFile;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TaskSchedulerManager {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerManager.class);
    
    // 任务失败计数器
    private final ConcurrentHashMap<String, AtomicInteger> taskFailureCounters = new ConcurrentHashMap<>();
    
    // 任务禁用状态
    private final ConcurrentHashMap<String, Boolean> taskDisabledStatus = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("schedulerExecutor")
    private ScheduledExecutorService schedulerExecutor;

    @Autowired
    @Qualifier("taskExecutor1")
    private ExecutorService taskExecutor1;

    @Autowired
    private Novelpia novelpia;

    @Autowired
    private NovelHtmlGenerator novelHtmlGenerator;

    @Autowired
    private NovelIndexGenerator novelIndexGenerator;

    @Autowired
    private NovelpiaCrawler novelpiaCrawler;

    @Autowired
    private UpdateNovelFromFile updateNovelFromFile;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    // 从application.properties读取配置（作为默认值）
    @Value("${task.scheduler.enabled:false}")
    private boolean schedulerEnabledDefault;

    @Value("${task.novelpia.task2.enabled:false}")
    private boolean task2EnabledDefault;

    @Value("${task.novelpia.photo.enabled:false}")
    private boolean photoEnabledDefault;

    @Value("${task.novelpia.upload.translation.enabled:false}")
    private boolean uploadTranslationEnabledDefault;

    @Value("${task.novelpia.upload.translation.exception.enabled:false}")
    private boolean uploadTranslationExceptionEnabledDefault;

    @Value("${task.novelpia.task3.enabled:false}")
    private boolean task3EnabledDefault;

    @Value("${task.novelpia.fix.error.chapter.enabled:false}")
    private boolean fixErrorChapterEnabledDefault;

    @Value("${task.sitemap.html.generator.enabled:false}")
    private boolean htmlGeneratorEnabledDefault;

    @Value("${task.sitemap.index.generator.enabled:false}")
    private boolean indexGeneratorEnabledDefault;

    @Value("${task.novelpia.crawler.enabled:false}")
    private boolean crawlerEnabledDefault;

    @Value("${task.update.novel.from.file.enabled:false}")
    private boolean updateNovelFromFileEnabledDefault;

    /**
     * 从数据库读取任务开关配置，如果不存在则使用配置文件默认值
     */
    private boolean getTaskSwitch(String key, boolean defaultValue) {
        try {
            Dictionary dict = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(key);
            if (dict != null) {
                return "true".equalsIgnoreCase(dict.getValueField());
            }
        } catch (Exception e) {
            log.warn("读取任务开关配置失败: {}, 使用默认值: {}", key, defaultValue);
        }
        return defaultValue;
    }

    @Value("${task.failure.threshold:3}")
    private int failureThreshold;

    @Value("${task.failure.reset.minutes:60}")
    private int failureResetMinutes;

    private volatile boolean isRunning = false;

    // 启动任务调度
    @PostConstruct // 应用启动后自动运行
    public void startScheduling() {
        if (!isRunning) {
            isRunning = true;
            schedulerExecutor.scheduleAtFixedRate(
                    this::submitTask,
                    120,
                    60,
                    TimeUnit.SECONDS
            );
        }
    }

    // 提交任务到线程池
    private void submitTask() {
        if (shouldExecute()) {
            // 每次执行时从数据库读取最新配置
            submitTaskIfEnabled("Novelpia-Task2", getTaskSwitch("task.novelpia.task2", task2EnabledDefault), this::executeTaskLogic2);
            submitTaskIfEnabled("Novelpia-Photo", getTaskSwitch("task.novelpia.photo", photoEnabledDefault), this::executePhoto);
            submitTaskIfEnabled("Novelpia-UploadTranslation", getTaskSwitch("task.novelpia.upload.translation", uploadTranslationEnabledDefault), this::executeUploadTranslation);
            submitTaskIfEnabled("Novelpia-UploadTranslationException", getTaskSwitch("task.novelpia.upload.translation.exception", uploadTranslationExceptionEnabledDefault), this::executeUploadTranslationException);
            submitTaskIfEnabled("Novelpia-Task3", getTaskSwitch("task.novelpia.task3", task3EnabledDefault), this::executeTaskLogic3);
            submitTaskIfEnabled("Novelpia-FixErrorChapter", getTaskSwitch("task.novelpia.fix.error.chapter", fixErrorChapterEnabledDefault), this::fixErrorChapter);
            submitTaskIfEnabled("HtmlGenerator", getTaskSwitch("task.sitemap.html.generator", htmlGeneratorEnabledDefault), this::executeHtmlGenerator);
            submitTaskIfEnabled("IndexGenerator", getTaskSwitch("task.sitemap.index.generator", indexGeneratorEnabledDefault), this::executeIndexGenerator);
            submitTaskIfEnabled("NovelpiaCrawler", getTaskSwitch("task.novelpia.crawler", crawlerEnabledDefault), this::executeCrawler);
            submitTaskIfEnabled("UpdateNovelFromFile", getTaskSwitch("task.update.novel.from.file", updateNovelFromFileEnabledDefault), this::executeUpdateNovelFromFile);
        }
    }

    /**
     * 提交任务
     */
    private void submitTaskIfEnabled(String taskName, boolean enabled, Runnable task) {
        if (!enabled) {
            return;
        }
        
        // 检查任务是否因连续失败而被禁用
        if (isTaskDisabled(taskName)) {
            log.warn("任务 {} 因连续失败 {} 次已被临时禁用,跳过本次执行", taskName, failureThreshold);
            return;
        }
        
        taskExecutor1.submit(() -> safeExecute(taskName, task));
    }

    /**
     * 安全执行任务
     * @param taskName 任务名称
     * @param task 任务执行逻辑
     */
    private void safeExecute(String taskName, Runnable task) {
        try {
            log.debug("开始执行任务: {}", taskName);
            task.run();
            log.debug("任务执行完成: {}", taskName);
            
            // 执行成功,重置失败计数
            resetFailureCount(taskName);
            
        } catch (Exception e) {
            handleTaskFailure(taskName, e);
        } catch (Throwable t) {
            handleTaskFailure(taskName, t);
        }
    }

    /**
     * 处理任务失败
     */
    private void handleTaskFailure(String taskName, Throwable throwable) {
        // 增加失败计数
        int failureCount = incrementFailureCount(taskName);
        
        // 判断是否需要禁用任务
        if (failureCount >= failureThreshold) {
            disableTask(taskName);
            log.error("任务 {} 连续失败{}次,已自动禁用。将在{}分钟后自动恢复。错误: {}", 
                    taskName, failureCount, failureResetMinutes, throwable.getMessage());
            
            // 仅记录简要错误信息,不打印完整堆栈
            if (log.isDebugEnabled()) {
                log.debug("任务 {} 详细错误堆栈:", taskName, throwable);
            }
            
            // 安排任务恢复
            scheduleTaskRecovery(taskName);
        } else {
            log.warn("任务 {} 执行失败 ({}/{}): {}", 
                    taskName, failureCount, failureThreshold, throwable.getMessage());
            
            // 仅在DEBUG级别打印堆栈
            if (log.isDebugEnabled()) {
                log.debug("任务 {} 错误详情:", taskName, throwable);
            }
        }
    }

    /**
     * 增加任务失败计数
     */
    private int incrementFailureCount(String taskName) {
        return taskFailureCounters
                .computeIfAbsent(taskName, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    /**
     * 重置任务失败计数
     */
    private void resetFailureCount(String taskName) {
        AtomicInteger counter = taskFailureCounters.get(taskName);
        if (counter != null && counter.get() > 0) {
            log.info("任务 {} 执行成功,重置失败计数", taskName);
            counter.set(0);
        }
    }

    /**
     * 检查任务是否被禁用
     */
    private boolean isTaskDisabled(String taskName) {
        return taskDisabledStatus.getOrDefault(taskName, false);
    }

    /**
     * 禁用任务
     */
    private void disableTask(String taskName) {
        taskDisabledStatus.put(taskName, true);
    }

    /**
     * 启用任务
     */
    private void enableTask(String taskName) {
        taskDisabledStatus.put(taskName, false);
    }

    /**
     * 安排任务恢复
     */
    private void scheduleTaskRecovery(String taskName) {
        schedulerExecutor.schedule(() -> {
            log.info("自动恢复任务: {} (失败计数已重置)", taskName);
            resetFailureCount(taskName);
            enableTask(taskName);
        }, failureResetMinutes, TimeUnit.MINUTES);
    }

    // 具体任务逻辑
    private void executeTaskLogic2() {
        novelpia.executeTask2();
    }
    // 具体任务逻辑
    private void executeTaskLogic3() {
        novelpia.executeTask3();
    }
    // 具体任务逻辑
    private void fixErrorChapter() {
        novelpia.fixErrorChapter();
    }
    // 具体任务逻辑
    private void executePhoto() {
        novelpia.photo();
    }
    // 具体任务逻辑
    private void executeUploadTranslation() {
        novelpia.executeUploadTranslation();
    }
    // 具体任务逻辑
    private void executeUploadTranslationException() {
        novelpia.executeUploadTranslationException();
    }

    // 小说HTML生成
    private void executeHtmlGenerator() {
        novelHtmlGenerator.execute();
    }

    // 索引页面生成
    private void executeIndexGenerator() {
        novelIndexGenerator.execute();
    }

    // Novelpia爬取
    private void executeCrawler() {
        novelpiaCrawler.execute();
    }

    // 从文件更新小说数据
    private void executeUpdateNovelFromFile() {
        updateNovelFromFile.execute();
    }

    // 动态控制执行条件
    private boolean shouldExecute() {
        return getTaskSwitch("task.scheduler", schedulerEnabledDefault);
    }

    // 停止调度
    public void stopScheduling() {
        isRunning = false;
        schedulerExecutor.shutdown();
    }
}