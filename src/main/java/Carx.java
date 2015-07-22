import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.AbstractOnSubscribe;
import rx.schedulers.Schedulers;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by maxim on 09-07-15.
 */

public class Carx {

    private final Observable<String> response = Observable.defer(new Func0<Observable<String>>() {
        final Random random = new Random(1000000);

        @Override
        public Observable<String> call() {
            final long value = random.nextLong();
            final String string = String.format("%d", value);
            System.out.println(String.format("response.defer().call() -> %s", string));
            return Observable.just(string);
        }
    });
    private final Observable<String> o = AbstractOnSubscribe.create(new Action1<AbstractOnSubscribe.SubscriptionState<String, Integer>>() {
        @Override
        public void call(AbstractOnSubscribe.SubscriptionState<String, Integer> subscriptionState) {
            System.out.println(String.format("state: %d", subscriptionState.state()));
            subscriptionState.onNext("Boo!");
            subscriptionState.onCompleted();
        }
    }).toObservable();
    private Subscriber<String> receiver = new Subscriber<String>() {
        @Override
        public void onCompleted() {
            System.out.println(String.format("receiver.onCompleted()"));
            unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            System.out.println(String.format("receiver.onError() -> %s", e.getMessage()));
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

    public void test1() {
        o.subscribe(receiver);
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

    public void test2() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        Observable.mergeDelayError(response, Observable.error(new Exception("Exception 1")), response, Observable.error(new Exception("Exception 2")))./*subscribeOn(Schedulers.computation()).observeOn(Schedulers.newThread()).*/subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {
                System.out.println(String.format("receiver.onCompleted()"));
                unsubscribe();
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(String.format("receiver.onError() -> %s", e.getMessage()));
                unsubscribe();
                latch.countDown();
            }

            @Override
            public void onNext(Object o) {
                System.out.println(String.format("receiver.onNext() -> %s", o.toString()));
            }
        });
        latch.await();
    }

    public static void main(String... args) throws Exception {
        System.out.println("Carx!");
        final Carx carx = new Carx();
        carx.test1();
        carx.test2();
    }
}
