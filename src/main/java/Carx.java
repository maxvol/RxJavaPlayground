import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

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

    public Carx() {

    }

    public void run() {
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

    }

    public static void main(String... args) {
        System.out.println("Carx!");
        final Carx carx = new Carx();
        carx.run();
    }
}
