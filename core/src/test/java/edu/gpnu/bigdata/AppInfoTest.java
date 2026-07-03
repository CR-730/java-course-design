package edu.gpnu.bigdata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppInfoTest {
    @Test
    void nameMatchesTopic() {
        assertEquals("用户行为漏斗分析", AppInfo.NAME);
    }
}

