package dev.melvinchia3636.airngreen.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

/**
 * Runs blocking work off the event-dispatch thread and delivers the result
 * back on it.
 */
public final class Async {

    private Async() {
    }

    /**
     * Starts background work.
     *
     * @param work    operation that may block or throw
     * @param success consumer called on the event-dispatch thread
     * @param failure consumer called when the work throws
     * @param <T>     result type
     */
    public static <T> void run(Callable<T> work, Consumer<T> success,
                               Consumer<Exception> failure) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                try {
                    success.accept(get());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    failure.accept(interrupted);
                } catch (ExecutionException failed) {
                    failure.accept(unwrap(failed));
                }
            }
        }.execute();
    }

    private static Exception unwrap(ExecutionException failed) {
        Throwable cause = failed.getCause();

        return cause instanceof Exception exception ? exception : failed;
    }
}
