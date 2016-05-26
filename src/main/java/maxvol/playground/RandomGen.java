package maxvol.playground;

import java.util.Random;

/**
 * Created by klm62830 on 26/05/16.
 */
public class RandomGen {

    private static final Random random = new Random();

    public static int generateInRange(final int from, final int to) {
        final long range = (long) to - (long) from + 1;
        // 0 <= number < range
        long number = (long) (range * random.nextDouble());
        final int result = (int) (number + from);
        return result;
    }

}