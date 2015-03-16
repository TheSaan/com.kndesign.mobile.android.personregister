package com.knoeflerdesign.keywest.Handler;

import java.util.Random;

/**
 * Created by Michael Knöfler on 09.03.2015.
 */
public class RandomHandler {
    public static int createIntegerFromRange(int aStart, int aEnd, Random aRandom){
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
        int randomNumber =  (int)(fraction + aStart);
        return randomNumber;
    }
}
