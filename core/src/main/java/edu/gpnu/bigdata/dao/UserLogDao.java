package edu.gpnu.bigdata.dao;

import edu.gpnu.bigdata.entity.UserLogRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserLogDao {
    public List<UserLogRecord> findAll(Connection connection) throws SQLException {
        String sql = """
                SELECT user_id, event_type, event_time, channel, device, product_category
                FROM user_log
                """;
        List<UserLogRecord> logs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                logs.add(new UserLogRecord(
                        resultSet.getLong("user_id"),
                        resultSet.getString("event_type"),
                        resultSet.getTimestamp("event_time").toLocalDateTime(),
                        resultSet.getString("channel"),
                        resultSet.getString("device"),
                        resultSet.getString("product_category")
                ));
            }
        }
        return logs;
    }
}

