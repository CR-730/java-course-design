package edu.gpnu.bigdata.util;

import com.zaxxer.hikari.HikariDataSource;
import edu.gpnu.bigdata.dao.UserLogDao;
import edu.gpnu.bigdata.dto.FunnelStats;
import edu.gpnu.bigdata.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

public final class ReportGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerator.class);

    private ReportGenerator() {
    }

    public static void main(String[] args) throws Exception {
        ConfigLoader config = new ConfigLoader();
        try (HikariDataSource dataSource = DataSourceFactory.create(config);
             Connection connection = dataSource.getConnection()) {
            var logs = new UserLogDao().findAll(connection);
            StatsService stats = new StatsService(logs);
            Path output = Path.of("report", "user-behavior-report.txt");
            Files.createDirectories(output.getParent());
            Files.writeString(output, buildReport(stats));
            LOGGER.info("Wrote report to {}", output.toAbsolutePath());
        }
    }

    static String buildReport(StatsService stats) {
        FunnelStats funnel = stats.funnelConversion();
        StringBuilder builder = new StringBuilder();
        builder.append("用户行为漏斗分析报表\n");
        builder.append("====================\n\n");
        builder.append("一、按事件类型统计\n");
        stats.countByEventType().forEach((key, value) -> builder.append(key).append(": ").append(value).append('\n'));
        builder.append("\n二、按渠道统计\n");
        stats.countByChannel().forEach((key, value) -> builder.append(key).append(": ").append(value).append('\n'));
        builder.append("\n三、漏斗转化率\n");
        builder.append("view 用户数: ").append(funnel.viewUsers()).append('\n');
        builder.append("cart 用户数: ").append(funnel.cartUsers()).append('\n');
        builder.append("order 用户数: ").append(funnel.orderUsers()).append('\n');
        builder.append("pay 用户数: ").append(funnel.payUsers()).append('\n');
        builder.append("view -> cart: ").append(funnel.viewToCartRate()).append("%\n");
        builder.append("cart -> order: ").append(funnel.cartToOrderRate()).append("%\n");
        builder.append("order -> pay: ").append(funnel.orderToPayRate()).append("%\n");
        builder.append("\n四、商品类别 Top 5\n");
        stats.topCategories(5).forEach((key, value) -> builder.append(key).append(": ").append(value).append('\n'));
        return builder.toString();
    }
}

