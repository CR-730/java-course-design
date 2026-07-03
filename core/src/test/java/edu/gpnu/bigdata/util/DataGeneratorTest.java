package edu.gpnu.bigdata.util;

import edu.gpnu.bigdata.entity.UserLogRecord;
import edu.gpnu.bigdata.entity.UserRecord;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataGeneratorTest {
    @Test
    void generatesSmallBatchInsideTransaction() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement deleteLogs = mock(PreparedStatement.class);
        PreparedStatement deleteUsers = mock(PreparedStatement.class);
        PreparedStatement insertUsers = mock(PreparedStatement.class);
        PreparedStatement insertLogs = mock(PreparedStatement.class);
        when(connection.prepareStatement("DELETE FROM user_log")).thenReturn(deleteLogs);
        when(connection.prepareStatement("DELETE FROM user")).thenReturn(deleteUsers);
        when(connection.prepareStatement(contains("INSERT INTO user ("))).thenReturn(insertUsers);
        when(connection.prepareStatement(contains("INSERT INTO user_log"))).thenReturn(insertLogs);

        SyntheticDataFactory factory = mock(SyntheticDataFactory.class);
        when(factory.userAt(1)).thenReturn(new UserRecord(1, "user_00001", "male", 19, "app"));
        when(factory.userAt(2)).thenReturn(new UserRecord(2, "user_00002", "female", 20, "web"));
        when(factory.logAt(1)).thenReturn(sampleLog(1));
        when(factory.logAt(2)).thenReturn(sampleLog(2));
        when(factory.logAt(3)).thenReturn(sampleLog(3));

        DataGenerator.generate(connection, factory, 2, 3);

        verify(connection).setAutoCommit(false);
        verify(deleteLogs).executeUpdate();
        verify(deleteUsers).executeUpdate();
        verify(insertUsers).setLong(1, 1L);
        verify(insertUsers).setString(2, "user_00001");
        verify(insertUsers).setString(3, "male");
        verify(insertUsers).setInt(4, 19);
        verify(insertUsers).setString(5, "app");
        verify(insertUsers, times(2)).addBatch();
        verify(insertUsers).executeBatch();
        verify(insertLogs, times(3)).setString(2, "view");
        verify(insertLogs, times(3)).setString(4, "app");
        verify(insertLogs, times(3)).addBatch();
        verify(insertLogs).executeBatch();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    void rollsBackAndRestoresAutoCommitWhenInsertFails() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement deleteLogs = mock(PreparedStatement.class);
        PreparedStatement deleteUsers = mock(PreparedStatement.class);
        PreparedStatement insertUsers = mock(PreparedStatement.class);
        when(connection.prepareStatement("DELETE FROM user_log")).thenReturn(deleteLogs);
        when(connection.prepareStatement("DELETE FROM user")).thenReturn(deleteUsers);
        when(connection.prepareStatement(contains("INSERT INTO user ("))).thenReturn(insertUsers);
        doThrow(new SQLException("write failed")).when(insertUsers).executeBatch();

        SyntheticDataFactory factory = mock(SyntheticDataFactory.class);
        when(factory.userAt(1)).thenReturn(new UserRecord(1, "user_00001", "male", 19, "app"));

        assertThrows(
                SQLException.class,
                () -> DataGenerator.generate(connection, factory, 1, 0)
        );

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
    }

    private static UserLogRecord sampleLog(int userId) {
        return new UserLogRecord(
                userId,
                "view",
                LocalDateTime.of(2026, 7, 3, 10, userId),
                "app",
                "android",
                "book"
        );
    }
}
