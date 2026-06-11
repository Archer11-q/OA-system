package com.oasystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * H2 数据库自增序列修复器
 * <p>
 * 问题背景：data.sql 使用显式 ID 插入种子数据（如 sys_dept id=1~5），
 * 但 H2 的 AUTO_INCREMENT 计数器不会被显式 INSERT 更新，
 * 导致后续无 ID 的 INSERT 生成与已有数据冲突的主键。
 * <p>
 * 解决方案：每次启动时动态查询各表当前最大 ID，
 * 将序列重置为 max(id) + 1，确保新插入的主键不会冲突。
 * <p>
 * 仅在 dev profile（H2 数据库）下激活。
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class H2SequenceInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    /** 需要重置序列的表名列表 */
    private static final String[] TABLES = {
        "sys_dept", "sys_user", "sys_role", "sys_menu",
        "sys_user_role", "sys_role_menu",
        "appr_template", "appr_instance", "appr_record",
        "att_record", "att_leave_request",
        "sys_notice", "sch_schedule", "exp_request", "sys_oper_log"
    };

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection()) {
            String dbProduct = conn.getMetaData().getDatabaseProductName();
            if (!"H2".equals(dbProduct)) {
                log.info("非 H2 数据库（{}），跳过序列修复", dbProduct);
                return;
            }

            log.info("开始修复 H2 自增序列...");
            int fixed = 0;
            for (String table : TABLES) {
                try (Statement stmt = conn.createStatement()) {
                    // 查询当前最大 ID
                    ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(id), 0) FROM " + table);
                    if (rs.next()) {
                        long maxId = rs.getLong(1);
                        long nextId = maxId + 1;
                        stmt.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + nextId);
                        log.debug("  {} → RESTART WITH {}", table, nextId);
                        fixed++;
                    }
                    rs.close();
                } catch (Exception e) {
                    // 表可能不存在或没有 id 列，跳过
                    log.debug("  跳过 {}: {}", table, e.getMessage());
                }
            }
            log.info("H2 序列修复完成，已处理 {}/{} 个表", fixed, TABLES.length);
        } catch (Exception e) {
            log.warn("H2 序列修复失败（非致命）: {}", e.getMessage());
        }
    }
}
