package com.example.gaokaoproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@RestController
public class DbInitController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/api/admin/seed-favorites")
    public String seedFavorites() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            // 清掉全部收藏数据，重新写入
            stmt.execute("DELETE FROM favorite");
            // 七所院校：985在前、211/双一流居中、普通在后，数量有梯度
            int[][] seeds = {
                {3, 6},  // 武汉大学 985
                {4, 5},  // 华中科技大学 985
                {5, 4},  // 武汉理工大学 211 ← 新增
                {6, 3},  // 华中农业大学 双一流
                {7, 2},  // 中南财经政法大学 双一流
                {2, 1},  // 中南民族大学 普通
                {1, 1},  // 武汉纺织大学 普通
            };
            int uid = 1000;
            for (int[] seed : seeds) {
                int instId = seed[0];
                int count = seed[1];
                for (int i = 0; i < count; i++) {
                    stmt.execute("INSERT INTO favorite(user_id, type, target_id, created_at) VALUES(" +
                            (uid++) + ", 1, " + instId + ", NOW())");
                }
                var rs = stmt.executeQuery("SELECT institution_name, level FROM institution WHERE id=" + instId);
                rs.next();
                sb.append("[收藏] ").append(rs.getString(1)).append(" (").append(rs.getString(2)).append(") ×").append(count).append("\n");
            }

            // 补湖北招生计划：给每所院校加一条湖北计划
            sb.append("\n=== 补湖北招生计划 ===\n");
            String[][] hubeiPlans = {
                {"3","080901","计算机科学与技术","物理"},
                {"4","100201K","临床医学","物理,化学"},
                {"5","080207","车辆工程","物理"},
                {"6","090101","农学","化学"},
                {"7","030101K","法学","不限"},
                {"2","030301","社会学","不限"},
                {"1","082001","纺织工程","物理,化学"},
            };
            for (String[] p : hubeiPlans) {
                try {
                    stmt.execute("INSERT INTO enrollment_plan(institution_id, year, province_code, batch_code, category_code, major_code, major_name, plan_count, status, created_by, created_at) " +
                            "VALUES(" + p[0] + ",2026,'420000','1','5','" + p[1] + "','" + p[2] + "',30,1,1,NOW())");
                    var rs = stmt.executeQuery("SELECT institution_name FROM institution WHERE id=" + p[0]);
                    rs.next();
                    sb.append("[OK] ").append(rs.getString(1)).append(" - ").append(p[2]).append(" (湖北)\n");
                } catch (Exception e) { sb.append("[失败] ").append(p[2]).append(": ").append(e.getMessage()).append("\n"); }
            }
        } catch (Exception e) {
            sb.append("[失败] ").append(e.getMessage());
        }
        return "<pre>" + sb.toString() + "</pre>";
    }

    @GetMapping("/api/admin/fix-seed-plans")
    public String fixSeedPlans() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            // {院校ID, 专业方向, 学费, 学位, 选科}
            Object[][] fixes = {
                {3, "人工智能与大数据方向", 5850, "工学", "物理"},
                {4, "全科医学方向", 6800, "医学", "物理,化学"},
                {5, "新能源汽车方向", 5000, "工学", "物理"},
                {6, "作物遗传育种方向", 4500, "农学", "化学"},
                {7, "民商法方向", 5200, "法学", "不限"},
                {2, "城乡社会研究方向", 4800, "法学", "不限"},
                {1, "航天纺织新材料方向", 5850, "工学", "物理,化学"},
            };
            for (Object[] f : fixes) {
                int instId = (int) f[0];
                String sql = "UPDATE enrollment_plan SET major_direction='" + f[1] + "', tuition_fee=" + f[2] +
                        ", schooling_length=4, degree_type='" + f[3] + "', education_level=1, limit_subjects='" + f[4] +
                        "', is_new_major=0, remark='运营端补录', published_at=NOW() " +
                        "WHERE institution_id=" + instId + " AND province_code='420000' AND created_by=1";
                int rows = stmt.executeUpdate(sql);
                var rs = stmt.executeQuery("SELECT institution_name FROM institution WHERE id=" + instId);
                rs.next();
                sb.append("[OK] ").append(rs.getString(1)).append(" 已更新 ").append(rows).append(" 条\n");
            }
        } catch (Exception e) { sb.append("[失败] ").append(e.getMessage()); }
        return "<pre>" + sb.toString() + "</pre>";
    }

    @GetMapping("/api/admin/check")
    public String check() {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM admission_guide");
            rs.next();
            return "admission_guide 表共 " + rs.getInt(1) + " 条记录";
        } catch (Exception e) {
            return "查询失败: " + e.getMessage();
        }
    }

    @GetMapping("/api/admin/init-db")
    public String initDb() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            try { stmt.execute("ALTER TABLE admission_guide ADD COLUMN review_status INT DEFAULT 0"); sb.append("[OK] review_status\n"); } catch (Exception e) { sb.append("[跳过] review_status: ").append(e.getMessage()).append("\n"); }
            try { stmt.execute("ALTER TABLE admission_guide ADD COLUMN review_comment VARCHAR(500)"); sb.append("[OK] review_comment\n"); } catch (Exception e) { sb.append("[跳过] review_comment: ").append(e.getMessage()).append("\n"); }
            try { stmt.execute("ALTER TABLE admission_guide ADD COLUMN reviewer_id BIGINT"); sb.append("[OK] reviewer_id\n"); } catch (Exception e) { sb.append("[跳过] reviewer_id: ").append(e.getMessage()).append("\n"); }
        } catch (Exception e) {
            sb.append("[致命] 建列失败: ").append(e.getMessage());
        }
        return "<pre>" + sb.toString() + "</pre>";
    }

    @GetMapping("/api/admin/insert-test")
    public String insertTest() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                // 先查表结构，列出所有列
                sb.append("=== admission_guide 表结构 ===\n");
                var rs = stmt.executeQuery("SHOW COLUMNS FROM admission_guide");
                while (rs.next()) sb.append(rs.getString("Field")).append(" | ").append(rs.getString("Type")).append("\n");
                rs.close();

                sb.append("\n=== 尝试插入第一条 ===\n");
                try {
                    stmt.executeUpdate("INSERT INTO admission_guide(institution_id, year, title, content, source_file_name, review_status, created_by, created_at) VALUES(1,2026,'清华大学2026年招生简章','计算机科学与技术专业招收120人，最低分680','清华简章.pdf',0,1,NOW())");
                    sb.append("[OK] 清华插入成功\n");
                } catch (Exception e) { sb.append("[失败] ").append(e.getMessage()).append("\n"); }

                sb.append("\n=== 尝试插入第二条 ===\n");
                try {
                    stmt.executeUpdate("INSERT INTO admission_guide(institution_id, year, title, content, source_file_name, review_status, created_by, created_at) VALUES(2,2026,'北京大学2026年招生简章','金融学专业招收80人，最低分675','北大简章.pdf',0,1,NOW())");
                    sb.append("[OK] 北大插入成功\n");
                } catch (Exception e) { sb.append("[失败] ").append(e.getMessage()).append("\n"); }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                sb.append("[回滚] ").append(e.getMessage()).append("\n");
            }
        } catch (Exception e) {
            sb.append("[致命] ").append(e.getMessage());
        }
        return "<pre>" + sb.toString() + "</pre>";
    }
}
