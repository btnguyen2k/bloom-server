package bloomfilter.engine;

/**
 * Abstract implementation of {@link IBloomFilter}.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public abstract class BaseBloomFilter implements IBloomFilter {

    private String name;
    private long expectedNumItems = 0;
    private double expectedFpp = 0.001;
    private int numHashFuncs = 0;

    public BaseBloomFilter() {
    }

    /*----------------------------------------------------------------------*/
    /**
     * Initialization method.
     * 
     * @return
     */
    public BaseBloomFilter init() {
        return this;
    }

    /**
     * Destroy method.
     */
    public void destroy() {
        // EMPTY
    }

    /*----------------------------------------------------------------------*/
    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Sets this bloomfilter's name.
     * 
     * @param name
     * @return
     */
    public BaseBloomFilter name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Alias of {@link #name()}.
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Alias of {@link #name(String)}.
     * 
     * @param name
     * @return
     */
    public BaseBloomFilter setName(String name) {
        this.name = name;
        return this;
    }

    // ------------------------------
    /**
     * Expected number of items to be put to this bloomfilter.
     * 
     * @return
     */
    public long expectedNumItems() {
        return expectedNumItems;
    }

    /**
     * Sets expected number of items to be put to this bloomfilter.
     * 
     * @param expectedNumItems
     * @return
     */
    public BaseBloomFilter expectedNumItems(long expectedNumItems) {
        this.expectedNumItems = expectedNumItems;
        return this;
    }

    /**
     * Alias of {@link #expectedNumItems()}.
     * 
     * @return
     */
    public long getExpectedNumItems() {
        return expectedNumItems;
    }

    /**
     * Alias of {@link #expectedNumItems(long)}.
     * 
     * @param expectedNumItems
     * @return
     */
    public BaseBloomFilter setExpectedNumItems(long expectedNumItems) {
        this.expectedNumItems = expectedNumItems;
        return this;
    }

    // ------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public double expectedFpp() {
        return expectedFpp;
    }

    /**
     * Sets the expected false-positive-probability value.
     * 
     * @param expectedFpp
     * @return
     */
    public BaseBloomFilter expectedFpp(double expectedFpp) {
        this.expectedFpp = expectedFpp;
        return this;
    }

    /**
     * Alias of {@link #expectedFpp()}.
     * 
     * @return
     */
    public double getExpectedFpp() {
        return expectedFpp;
    }

    /**
     * Alias of {@link #expectedFpp(double)}.
     * 
     * @param expectedFpp
     * @return
     */
    public BaseBloomFilter setExpectedFpp(double expectedFpp) {
        this.expectedFpp = expectedFpp;
        return this;
    }

    // ------------------------------
    /**
     * Number of hash functions.
     * 
     * @return
     */
    public int numHashFuncs() {
        return numHashFuncs;
    }

    /**
     * Sets number of hash functions.
     * 
     * @param numHashFuncs
     * @return
     */
    public BaseBloomFilter numHashFuncs(int numHashFuncs) {
        this.numHashFuncs = numHashFuncs;
        return this;
    }

    /**
     * Alias of {@link #numHashFuncs()}.
     * 
     * @return
     */
    public int getNumHashFuncs() {
        return numHashFuncs;
    }

    /**
     * Alias of {@link #numHashFuncs(int)}.
     * 
     * @param numHashFuncs
     * @return
     */
    public BaseBloomFilter setNumHashFuncs(int numHashFuncs) {
        this.numHashFuncs = numHashFuncs;
        return this;
    }

    /**
     * Return number of bits required by this bloom filter.
     * 
     * @return
     * @since 0.1.1
     */
    public long size() {
        double m = Math.ceil((expectedNumItems * Math.log(expectedFpp))
                / Math.log(1.0 / (Math.pow(2.0, Math.log(2.0)))));
        long numBits = (long) m;
        return numBits;
    }
}
