package org.omn3s.userservice.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omn3s.userservice.utils.SimpleRateCounter;

import java.time.Instant;

class SimpleRateCounterTest {

    @Test
    void testNegativeThrowsException() {
        SimpleRateCounter counter = new SimpleRateCounter();
        Assertions.assertThrows(IllegalArgumentException.class, () -> counter.increment(-100, "fo", "ba"));
    }

    @Test
    void testIncrement() {

        SimpleRateCounter counter = new SimpleRateCounter();
        String key = "foo";
        String altkey = "altfoo";
        long timestamp = Instant.parse("2024-12-25T00:00:00.00Z").toEpochMilli();

        Assertions.assertEquals(1, counter.increment(timestamp, key));
        Assertions.assertEquals(2, counter.increment(timestamp, key));
        Assertions.assertEquals(1, counter.increment(timestamp, altkey));
        Assertions.assertEquals(3, counter.increment(timestamp, key));

        // Add half bucket
        timestamp += counter.getResolutionMilliseconds() / 2;
        Assertions.assertEquals(4, counter.increment(timestamp, key));
        Assertions.assertEquals(5, counter.increment(timestamp, key));
        Assertions.assertEquals(6, counter.increment(timestamp, key));
        // Add half bucket
        timestamp += counter.getResolutionMilliseconds() / 2;
        Assertions.assertEquals(1, counter.increment(timestamp, key));
        Assertions.assertEquals(2, counter.increment(timestamp, key));
        Assertions.assertEquals(1, counter.increment(timestamp, altkey));

    }
}