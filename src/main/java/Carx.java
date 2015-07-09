import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.Random;

/**
 * Created by maxim on 09-07-15.
 */

public class Carx {

    private Observable<String> response = Observable.defer(new Func0<Observable<String>>() {
        final Random random = new Random(1000000);
        @Override
        public Observable<String> call() {
            final long value = random.nextLong();
            final String string = String.format("%d", value);
            System.out.println(String.format("response.defer().call() -> %s", string));
            return Observable.just(string);
        }
    });
    private Subscriber<String> receiver = new Subscriber<String>() {
        @Override
        public void onCompleted() {
            System.out.println(String.format("receiver.onCompleted()"));
            unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            System.out.println(String.format("receiver.onError()"));
            unsubscribe();
        }

        @Override
        public void onNext(String s) {
            System.out.println(String.format("receiver.onNext() -> %s", s));
            unsubscribe();
        }
    };

    public Carx() {

    }

    public void run() {
        response.subscribe(receiver);
        response.subscribe(receiver);
        response.subscribe(receiver);
        response.subscribe(receiver);
        response.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(String.format("s1 -> %s", s));
            }
        });
        response.publish().connect();
        response.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(String.format("s2 -> %s", s));
            }
        });
        response.publish().connect();
        final Observable<String> o1 = response.publish(new Func1<Observable<String>, Observable<String>>() {
            @Override
            public Observable<String> call(Observable<String> stringObservable) {
                return stringObservable;
            }
        });
    }

    public static void main(String... args) {
        System.out.println("Carx!");
        final Carx carx = new Carx();
        carx.run();
    }
}
