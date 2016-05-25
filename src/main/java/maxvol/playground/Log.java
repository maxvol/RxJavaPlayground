package maxvol.playground;

/**
 *
 */
public class Log {

    public static void d(final String message) {
        System.err.println(message);
    }

    public static void d(final Throwable t) {
        d(String.format("%s", t.toString()));
    }

}
