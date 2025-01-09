package org.omn3s.userservice.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleRateCounter {
    private final long resolutionMilliseconds;

    private final AtomicReference<TimeBucket> currentBucket = new AtomicReference<>(new TimeBucket(0));

    public SimpleRateCounter() {
        this(60 * 1000L);
    }

    public SimpleRateCounter(long resolutionMilliseconds) {
        this.resolutionMilliseconds = resolutionMilliseconds;
    }

    public long getResolutionMilliseconds() {
        return resolutionMilliseconds;
    }

    /**
     * Return the relevant TimeBucket for the specified time.
     * Assuming a buckets of 1 minute. A bucket is relevant if it is in the same minute
     * or later than the provided time.
     *
     * @param epochMillis - time in millis seconds
     * @return relevant time bucket which is same or later that the provided time.
     */
    protected TimeBucket timeBucketFor(long epochMillis) {
        if (epochMillis < 0) throw new IllegalArgumentException("Time should not be negative");
        long timeBucketKey = epochMillis / resolutionMilliseconds;
        TimeBucket currentLimits = currentBucket.get();
        // Current Bucket is valid return current limiter
        if (currentLimits.timeBucket >= timeBucketKey) return currentLimits;
        TimeBucket newBucket = new TimeBucket(timeBucketKey);
        do {
            currentLimits = currentBucket.get();
            //
            if (currentLimits.timeBucket >= newBucket.timeBucket)
                return currentLimits;
        } while (!this.currentBucket.compareAndSet(currentLimits, newBucket));
        return currentBucket.get();
    }

    protected String keyFor(String user, String resource) {
        return user + "::" + resource;
    }

    public long increment(long epochMillis, String key) {
        return timeBucketFor(epochMillis).increment(key);
    }

    public long increment(long epochMillis, String user, String resource) {
        String key = keyFor(user, resource);
        return increment(epochMillis, key);
    }


    public static class TimeBucket {
        final long timeBucket;
        final ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<>();

        public TimeBucket(long timeBucket) {
            this.timeBucket = timeBucket;
        }

        public AtomicLong countFor(String key) {
            return counts.computeIfAbsent(key, (k) -> new AtomicLong());
        }

        public long increment(String key) {
            return countFor(key).incrementAndGet();
        }
    }


}
