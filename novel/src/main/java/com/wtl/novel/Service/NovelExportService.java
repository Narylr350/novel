package com.wtl.novel.Service;

import com.wtl.novel.entity.Novel;
import com.wtl.novel.repository.NovelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.zip.GZIPInputStream;

/**
 * 小说导出服务
 * 使用原生 JDBC 直接读取压缩内容并解压，生成 TXT 文件
 */
@Service
public class NovelExportService {

    private static final Logger log = LoggerFactory.getLogger(NovelExportService.class);

    private final DataSource dataSource;

    @Autowired
    private NovelRepository novelRepository;

    public NovelExportService(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 导出小说为TXT内容
     * @param novelId 小说ID
     * @return TXT文件内容的字节数组，如果失败返回null
     */
    public byte[] exportNovelToTxt(Long novelId) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            log.error("小说不存在: {}", novelId);
            return null;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT chapter_number, title, content FROM chapter WHERE novel_id = ? AND is_deleted = 0 ORDER BY chapter_number")) {

            ps.setLong(1, novelId);
            
            StringBuilder txtContent = new StringBuilder();
            txtContent.append("【").append(novel.getTitle()).append("】\n");
            txtContent.append("==================================================\n\n");

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    int chapterNumber = rs.getInt("chapter_number");
                    String chapterTitle = rs.getString("title");
                    byte[] compressedContent = rs.getBytes("content");
                    
                    // 解压内容
                    String content = decompress(compressedContent);
                    content = cleanContent(content);

                    txtContent.append("第").append(chapterNumber).append("章 ");
                    txtContent.append(chapterTitle).append("\n");
                    txtContent.append("----------------------------------------\n");
                    txtContent.append(content);
                    txtContent.append("\n\n");
                    count++;
                }
                
                if (count == 0) {
                    log.warn("小说没有章节: {} (ID: {})", novel.getTitle(), novelId);
                    return null;
                }
                
                log.info("导出小说成功: {} (ID: {}), 共 {} 章", novel.getTitle(), novelId, count);
            }

            return txtContent.toString().getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("导出小说失败: {} (ID: {})", novel.getTitle(), novelId, e);
            return null;
        }
    }

    /**
     * 获取导出文件名
     */
    public String getExportFileName(Long novelId) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            return "unknown_" + novelId + ".txt";
        }
        return sanitizeFileName(novel.getTitle()) + "_" + novelId + ".txt";
    }

    /**
     * 解压 GZIP 内容
     */
    private String decompress(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) return "";
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
             GZIPInputStream gzip = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8);
        } catch (java.util.zip.ZipException e) {
            // 如果不是 GZIP 格式，尝试直接作为文本返回
            return new String(compressed, StandardCharsets.UTF_8);
        }
    }

    /**
     * 清理内容中的特殊字符
     */
    private String cleanContent(String content) {
        if (content == null) return "";
        return content.replaceAll("[\u200B\u200C\u200D\uFEFF]", "");
    }

    /**
     * 清理文件名中的非法字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unknown";
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
