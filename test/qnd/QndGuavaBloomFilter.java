package qnd;

import java.text.MessageFormat;

import bloomfilter.engine.GuavaBloomFilter;
import bloomfilter.engine.IBloomFilter;

public class QndGuavaBloomFilter {

    private static void runTest(long expectedNumItems, long numItems, double fpp) {
        System.out.println("==================================================");
        System.out.println(MessageFormat.format("Run test [{0}/{1}/{2}]", expectedNumItems,
                numItems, fpp));
        System.out.println("==================================================");
        IBloomFilter bloomFilter = new GuavaBloomFilter().expectedNumItems(expectedNumItems)
                .expectedFpp(fpp).init();

        long numErrors = 0;
        for (long i = 1; i <= numItems; i++) {
            byte[] item = String.valueOf(i).getBytes();
            if (!bloomFilter.put(item)) {
                numErrors++;
            }
        }
        System.out.println("Errors:" + (double) numErrors / (double) numItems);

        long numFalses = 0;
        for (long i = numItems + 1; i <= expectedNumItems * 2; i++) {
            byte[] item = String.valueOf(i).getBytes();
            if (bloomFilter.mightContain(item)) {
                numFalses++;
            }
        }
        System.out.println("Failses:" + (double) numFalses
                / (double) (expectedNumItems * 2 - numItems));

        System.out.println();
    }

    public static void main(String[] args) {
        runTest(10000, 10000 - 10, 1E-3);
        runTest(10000, 10000 - 10, 1E-6);
        runTest(10000000, 10000000 - 10, 1E-3);
        runTest(10000000, 10000000 - 10, 1E-6);
        // runTest(10000000, 10000000 - 10, 1E-27);
    }

}
