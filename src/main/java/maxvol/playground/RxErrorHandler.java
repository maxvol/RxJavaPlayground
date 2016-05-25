package maxvol.playground;

import rx.annotations.Beta;
import rx.plugins.RxJavaErrorHandler;

/**
 *
 */
public class RxErrorHandler extends RxJavaErrorHandler {

    @Override
    public void handleError(Throwable t) {
        final Thread thread = Thread.currentThread();
        final String threadInfo = String.format("[thread: #%d '%s']", thread.getId(), thread.getName());
        Log.d(threadInfo);
        Log.d(t);
    }

    @Override
    @Beta
    protected String render(Object item) throws InterruptedException {
        final String rendered = String.format("%s", item);
        // TODO: implement rendering for custom data classes
        return rendered;
    }

}
