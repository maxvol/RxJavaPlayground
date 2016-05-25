package maxvol.playground;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.plugins.RxJavaObservableExecutionHook;

/**
 *
 */
public class RxObservableExecutionHook extends RxJavaObservableExecutionHook {

    private static final String PACKAGE = "maxvol.playground.";

    @Override
    public <T> Observable.OnSubscribe<T> onCreate(Observable.OnSubscribe<T> f) {
//        maxvol.playground.Log.d(logEntryWithStack(String.format("%s", f.getClass().getName())));
        return super.onCreate(f);
    }

    @Override
    public <T> Observable.OnSubscribe<T> onSubscribeStart(Observable<? extends T> observableInstance, Observable.OnSubscribe<T> onSubscribe) {
//        maxvol.playground.Log.d(logEntryWithStack(String.format("%s %s", observableInstance.getClass().getName(), onSubscribe.getClass().getName())));
        return super.onSubscribeStart(observableInstance, onSubscribe);
    }

    @Override
    public <T> Subscription onSubscribeReturn(Subscription subscription) {
//        Log.d(logEntryWithStack(String.format("%s", subscription.getClass().getName())));
        return super.onSubscribeReturn(subscription);
    }

    @Override
    public <T> java.lang.Throwable onSubscribeError(java.lang.Throwable e) {
        Log.d(logEntryWithStack(String.format("%s", e.getMessage())));
        return super.onSubscribeError(e);
    }

    @Override
    public <T, R> Observable.Operator<? extends R, ? super T> onLift(Observable.Operator<? extends R, ? super T> lift) {
//        maxvol.playground.Log.d(logEntryWithStack(String.format("%s", lift.getClass().getName())));
        return super.onLift(lift);
    }

    public static String logEntryWithStack(final String firstLine) {
        final Thread thread = Thread.currentThread();
        final String threadInfo = String.format("[thread: #%d '%s']", thread.getId(), thread.getName());
        final Observable<String> lines = Observable.from(thread.getStackTrace()).filter(new Func1<StackTraceElement, Boolean>() {
            @Override
            public Boolean call(StackTraceElement stackTraceElement) {
                final String className = stackTraceElement.getClassName();
                return (className.startsWith(PACKAGE) && !className.contains("_") && !className.endsWith(RxObservableExecutionHook.class.getName()));
            }
        }).map(new Func1<StackTraceElement, String>() {
            @Override
            public String call(StackTraceElement stackTraceElement) {
                return String.format("%s.%s() %s:%d", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getFileName(), stackTraceElement.getLineNumber());
            }
        }).startWith(firstLine, threadInfo);
//        final String logEntry = lines.collect(new Func0<StringBuilder>() {
//            @Override
//            public StringBuilder call() {
//                return new StringBuilder();
//            }
//        }, new Action2<StringBuilder, String>() {
//            @Override
//            public void call(StringBuilder stringBuilder, String string) {
//                stringBuilder.append(string);
//                stringBuilder.append("\n");
//            }
//        }).map(new Func1<StringBuilder, String>() {
//            @Override
//            public String call(StringBuilder stringBuilder) {
//                return stringBuilder.toString();
//            }
//        }).toBlocking().single();
//        return logEntry;
        return threadInfo;
    }

}
