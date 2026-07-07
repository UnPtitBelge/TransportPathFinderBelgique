package com.ulb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chronomètre une phase d'exécution (try-with-resources) et logge sa durée
 * et la variation de mémoire utilisée à la fermeture.
 */
public final class Profiler implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Profiler.class);

    private final String phase;
    private final long startNanos;
    private final long startMemoryBytes;

    private Profiler(String phase) {
        this.phase = phase;
        this.startMemoryBytes = usedMemoryBytes();
        this.startNanos = System.nanoTime();
        logger.debug("> {}", phase);
    }

    public static Profiler start(String phase) {
        return new Profiler(phase);
    }

    @Override
    public void close() {
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        long memoryDeltaMb = (usedMemoryBytes() - startMemoryBytes) / (1024 * 1024);
        logger.info("< {} — {} ms (mémoire: {}{} MB)", phase, durationMs,
                memoryDeltaMb >= 0 ? "+" : "", memoryDeltaMb);
    }

    public static long usedMemoryBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static void logEnvironment() {
        Runtime runtime = Runtime.getRuntime();
        logger.info("Environnement: {} coeurs disponibles, mémoire max {} MB",
                runtime.availableProcessors(), runtime.maxMemory() / (1024 * 1024));
    }
}
