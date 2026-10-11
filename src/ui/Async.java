package ui;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Runs a (possibly slow) database call off the Swing thread so the window never freezes,
 * then hands the result back on the Swing thread. Errors are logged to the console only.
 */
public final class Async {
    private Async() { }

    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception { return work.call(); }

            @Override
            protected void done() {
                T result;
                try {
                    result = get();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    System.err.println("[VRS] background task failed: " + cause);
                    cause.printStackTrace();
                    if (onError != null) onError.accept(cause instanceof Exception ? (Exception) cause : e);
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                onSuccess.accept(result);
            }
        }.execute();
    }
}
