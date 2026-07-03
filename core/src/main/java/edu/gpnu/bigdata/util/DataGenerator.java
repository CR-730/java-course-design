package edu.gpnu.bigdata.util;

import com.zaxxer.hikari.HikariDataSource;
import edu.gpnu.bigdata.entity.UserLogRecord;
import edu.gpnu.bigdata.entity.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class DataGenerator {
    public static final int USER_COUNT = 50_000;
    public static final int LOG_COUNT = 100_000;
    private static final int BATCH_SIZE = 1_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataGenerator.class);

    private DataGenerator() {
    }

    public static void main(String[] args) throws SQLException {
        ConfigLoader config = new ConfigLoader();
        try (HikariDataSource dataSource = DataSourceFactory.create(config);
             Connection connection = dataSource.getConnection()) {
            generate(connection);
        }
    }

    public static void generate(Connection connection) throws SQLException {
        generate(connection, new SyntheticDataFactory(USER_COUNT), USER_COUNT, LOG_COUNT);
    }

    static void generate(
            Connection connection,
            SyntheticDataFactory factory,
            int userCount,
            int logCount
    ) throws SQLException {
        connection.setAutoCommit(false);
        try {
            clearTables(connection);
            insertUsers(connection, factory, userCount);
            insertLogs(connection, factory, logCount);
            connection.commit();
            LOGGER.info("Generated {} users and {} behavior logs", userCount, logCount);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void clearTables(Connection connection) throws SQLException {
        try (PreparedStatement deleteLogs = connection.prepareStatement("DELETE FROM user_log");
             PreparedStatement deleteUsers = connection.prepareStatement("DELETE FROM user")) {
            deleteLogs.executeUpdate();
            deleteUsers.executeUpdate();
        }
    }

    private static void insertUsers(
            Connection connection,
            SyntheticDataFactory factory,
            int userCount
    ) throws SQLException {
        String sql = """
                INSERT INTO user (id, username, gender, age, register_channel)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= userCount; i++) {
                UserRecord user = factory.userAt(i);
                statement.setLong(1, user.id());
                statement.setString(2, user.username());
                statement.setString(3, user.gender());
                statement.setInt(4, user.age());
                statement.setString(5, user.registerChannel());
                statement.addBatch();
                executeEveryBatch(statement, i);
            }
            statement.executeBatch();
        }
    }

    private static void insertLogs(
            Connection connection,
            SyntheticDataFactory factory,
            int logCount
    ) throws SQLException {
        String sql = """
                INSERT INTO user_log (user_id, event_type, event_time, channel, device, product_category)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= logCount; i++) {
                UserLogRecord log = factory.logAt(i);
                statement.setLong(1, log.userId());
                statement.setString(2, log.eventType());
                statement.setTimestamp(3, Timestamp.valueOf(log.eventTime()));
                statement.setString(4, log.channel());
                statement.setString(5, log.device());
                statement.setString(6, log.productCategory());
                statement.addBatch();
                executeEveryBatch(statement, i);
            }
            statement.executeBatch();
        }
    }

    private static void executeEveryBatch(PreparedStatement statement, int index) throws SQLException {
        if (index % BATCH_SIZE == 0) {
            statement.executeBatch();
            statement.clearBatch();
        }
    }
}
