package com.polyglotmesh.runtime;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ExecutionTimeoutManager implements AutoCloseable {

    private final ExecutorService executor;

    public ExecutionTimeoutManager() {
        this(Executors.newCachedThreadPool());
    }

    ExecutionTimeoutManager(ExecutorService executor) {
        this.executor = executor;
    }

    public <T> T execute(Callable<T> action, long timeoutMillis) throws Exception {
        Future<T> future = executor.submit(action);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            future.cancel(true);
            throw exception;
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception checked) {
                throw checked;
            }
            throw new ExecutionException(cause);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
