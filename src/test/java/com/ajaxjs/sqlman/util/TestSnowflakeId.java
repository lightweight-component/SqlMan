package com.ajaxjs.sqlman.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSnowflakeId {
    @Test
    void staticGeneratorProducesUniquePositiveIds() {
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++)
            ids.add(SnowflakeId.get());

        assertEquals(1000, ids.size());
        assertTrue(ids.stream().allMatch(id -> id > 0));
    }

    @Test
    void workerGeneratorProducesStrictlyIncreasingIds() {
        SnowflakeId worker = new SnowflakeId(1);
        long previous = worker.nextId();

        for (int i = 0; i < 1000; i++) {
            long current = worker.nextId();
            assertTrue(current > previous);
            previous = current;
        }
    }

    @Test
    void rejectsWorkerIdOutsideThreeBitRange() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeId(-1));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeId(8));
    }
}
