package qnd;

public class QndBloomFilterSize {

    public static void main(String[] args) {
        long NUM_ITEMS = 1000000;
        double FPP = 1E-06;

        double m = Math.ceil((NUM_ITEMS * Math.log(FPP))
                / Math.log(1.0 / (Math.pow(2.0, Math.log(2.0)))));
        System.out.println(m);

    }
}
