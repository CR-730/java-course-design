package edu.gpnu.bigdata.dao;

import edu.gpnu.bigdata.entity.UserLogRecord;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserLogDaoTest {
    @Test
    void mapsResultSetRowsToUserLogRecords() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        LocalDateTime time = LocalDateTime.of(2026, 7, 3, 12, 30);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.contains("FROM user_log"))).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("user_id")).thenReturn(7L);
        when(resultSet.getString("event_type")).thenReturn("pay");
        when(resultSet.getTimestamp("event_time")).thenReturn(Timestamp.valueOf(time));
        when(resultSet.getString("channel")).thenReturn("app");
        when(resultSet.getString("device")).thenReturn("ios");
        when(resultSet.getString("product_category")).thenReturn("book");

        List<UserLogRecord> logs = new UserLogDao().findAll(connection);

        assertEquals(1, logs.size());
        assertEquals(new UserLogRecord(7L, "pay", time, "app", "ios", "book"), logs.get(0));
        verify(resultSet).close();
        verify(statement).close();
    }
}
