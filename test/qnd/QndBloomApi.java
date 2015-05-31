//package qnd;
//
//import java.text.MessageFormat;
//
//import api.BloomApi;
//
//public class QndBloomApi {
//
//    private static void runTest(BloomApi bloomApi, String bloomName, long expectedNumItems,
//            long numItems, double fpp) {
//        System.out.println("==================================================");
//        System.out.println(MessageFormat.format("Run test [{0}/{1}/{2}] - {3}", expectedNumItems,
//                numItems, fpp, bloomName));
//        System.out.println("==================================================");
//
//        bloomApi.initBloomFilter(bloomName, false, expectedNumItems, fpp);
//
//        long numErrors = 0;
//        for (long i = 1; i <= numItems; i++) {
//            byte[] item = String.valueOf(i).getBytes();
//            if (!bloomApi.put(bloomName, item)) {
//                numErrors++;
//            }
//        }
//        System.out.println("Errors:" + (double) numErrors / (double) numItems);
//
//        long numFalses = 0;
//        for (long i = numItems + 1; i <= expectedNumItems * 2; i++) {
//            byte[] item = String.valueOf(i).getBytes();
//            if (bloomApi.mightContain(bloomName, item)) {
//                numFalses++;
//            }
//        }
//        System.out.println("Failses:" + (double) numFalses
//                / (double) (expectedNumItems * 2 - numItems));
//
//        System.out.println();
//    }
//
//    public static void main(String[] args) throws InterruptedException {
//        BloomApi bloomApi = new BloomApi();
//        bloomApi.setStorageBasePath("/tmp/bloomfilter").init();
//        runTest(bloomApi, null, 10000, 10000 - 10, 1E-3);
//        runTest(bloomApi, "", 10000, 10000 - 10, 1E-6);
//        runTest(bloomApi, "1mil3", 10000000, 10000000 - 10, 1E-3);
//        runTest(bloomApi, "1mil6", 10000000, 10000000 - 10, 1E-6);
//        Thread.sleep(5000);
//    }
//
// }
