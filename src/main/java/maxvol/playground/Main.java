package maxvol.playground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javafx.util.Pair;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.AbstractOnSubscribe;
import rx.plugins.RxJavaPlugins;
import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;

/**
 * Created by maxim on 09-07-15.
 */
public class Main {

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

    public Main() {
    }

    private static void initDebugLoggingForSwallowedExceptions() {
        final RxJavaPlugins rxJavaPlugins = RxJavaPlugins.getInstance();
        rxJavaPlugins.registerErrorHandler(new RxErrorHandler());
        rxJavaPlugins.registerObservableExecutionHook(new RxObservableExecutionHook());
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

    public void test3() {
        // observer will receive "three" as the only onNext event.
        final AsyncSubject<String> subject = AsyncSubject.create();
        subject.subscribe(receiver);
        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onError(new Throwable("boo!"));
        subject.onCompleted();
    }

    public void test4() {
        final AsyncSubject<String> subject = AsyncSubject.create();
        subject.onErrorReturn(new Func1<Throwable, String>() {
            @Override
            public String call(Throwable throwable) {
                return throwable.getMessage();
            }
        }).subscribe(receiver);
        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onError(new Throwable("boo!"));
        subject.onCompleted();
    }

    public void test5() {
        // observer will receive "three" as the only onNext event.
        final PublishSubject<String> subject = PublishSubject.create();
        subject.takeLast(1).subscribe(receiver);
        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onCompleted();
    }

    public void testScan() {
        final List<String> simple = Observable.<String>just("a", "b", "c", "d", "e", "f").scan("_", new Func2<String, String, String>() {
            @Override
            public String call(String lhs, String rhs) {
                return lhs + rhs;
            }
        }).toList().toBlocking().single();
        Log.d(String.format("%s", simple));

        final int number = 9;
        // [0,] 1, 1, 2, 3, 5, 8, 13, 21, 34
        final Observable<Integer> fibonacci = Observable.just(0).repeat().scan(new Pair<Integer, Integer>(0, 1), new Func2<Pair<Integer, Integer>, Integer, Pair<Integer, Integer>>() {
            @Override
            public Pair<Integer, Integer> call(Pair<Integer, Integer> pair, Integer integer) {
//                Log.d(String.format("pair: %s, int: %s", pair, integer));
                int lhs = pair.getValue();
                int rhs = pair.getKey() + lhs;
                return new Pair<Integer, Integer>(lhs, rhs);
            }
        }).map(new Func1<Pair<Integer, Integer>, Integer>() {
            @Override
            public Integer call(Pair<Integer, Integer> pair) {
//                Log.d(String.format("map: %s", pair));
                return pair.getValue();
            }
        });
        final List<Integer> result = fibonacci.take(number).toList().toBlocking().single();
        Log.d(String.format("%s", result));
    }

    public void testReduce() {
        final int number = 5;
        // The value of 0! is 1, according to the convention for an empty product.
        // 5! = 5*4*3*2*1 = 120
        final int factorial = Observable.<Integer>range(1, number).reduce(new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer lhs, Integer rhs) {
//                Log.d(String.format("lhs: %d, rhs: %d", lhs, rhs));
                return lhs * rhs;
            }
        }).toBlocking().single();
        Log.d(String.format("%d", factorial));
    }

    public void testCollect() {
        final int number = 5;
        final String result = Observable.<Integer>range(1, number).collect(new Func0<StringBuilder>() {
            @Override
            public StringBuilder call() {
//                Log.d("new StringBuilder()");
                return new StringBuilder();
            }
        }, new Action2<StringBuilder, Integer>() {
            @Override
            public void call(StringBuilder stringBuilder, Integer integer) {
                stringBuilder.append(String.format("%s, ", integer));
            }
        }).map(new Func1<StringBuilder, String>() {
            @Override
            public String call(StringBuilder stringBuilder) {
                return stringBuilder.toString();
            }
        }).toBlocking().single();
        Log.d(String.format("%s", result));
    }

    public void testFlatMap() {
        final int number = 5;
        final List<String> result = Observable.<Integer>range(1, 5).flatMap(new Func1<Integer, Observable<String>>() {
            @Override
            public Observable<String> call(Integer integer) {
                final int count = RandomGen.generateInRange(0, 5);
                final String[] array = new String[count];
                Arrays.fill(array, integer.toString());
                return Observable.from(array);
            }
        }).toList().toBlocking().single();
        Log.d(String.format("%s", result));
    }

    public void testCollect2() {
        class Page {
            public List<String> lines = new ArrayList<String>();
            public int total = 10;

            public Page(final int index) {
                for (int i = 0; i < 5; i++) {
                    this.lines.add(String.format("%d", index * 10 + i));
                }
            }
        }
        class PageFactory {
            public Observable<Page> pageAtIndex(final int index) {
                return Observable.defer(new Func0<Observable<Page>>() {
                    @Override
                    public Observable<Page> call() {
                        return Observable.just(new Page(index));
                    }
                });
            }
        }
        final PageFactory pageFactory = new PageFactory();
        final Observable<Page> firstPage = pageFactory.pageAtIndex(0);
        final Observable<Page> otherPages = firstPage.flatMap(new Func1<Page, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Page page) {
                return Observable.<Integer>range(1, page.total - 1);
            }
        }).concatMap(new Func1<Integer, Observable<? extends Page>>() {
            @Override
            public Observable<? extends Page> call(Integer integer) {
                return pageFactory.pageAtIndex(integer);
            }
        });
        final Observable<Page> allPages = firstPage.concatWith(otherPages);
        final List<String> result = allPages.concatMap(new Func1<Page, Observable<String>>() {
            @Override
            public Observable<String> call(Page page) {
                return Observable.from(page.lines);
            }
        }).collect(new Func0<List<String>>() {
            @Override
            public List<String> call() {
                return new ArrayList<String>();
            }
        }, new Action2<List<String>, String>() {
            @Override
            public void call(List<String> strings, String string) {
                strings.add(string);
            }
        }).toBlocking().single();
        Log.d(String.format("%s", result));
    }

    public static void main(String... args) throws Exception {
        System.out.println("RxJava playground!");
        initDebugLoggingForSwallowedExceptions();
        final Main main = new Main();
        main.test5();
        main.testScan();
        main.testReduce();
        main.testCollect();
        main.testFlatMap();
        main.testCollect2();
    }

}
