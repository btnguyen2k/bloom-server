package bloomfilter.engine;

/**
 * BloomFilter interface.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public interface IBloomFilter {
    /**
     * Puts an item to the bloomfilter.
     * 
     * @param item
     * @return
     */
    public boolean put(byte[] item);

    /**
     * Returns {@code true} if the item might have been put in the bloomfilter,
     * {@code false} if it is definitely not the case.
     * 
     * @param item
     * @return
     */
    public boolean mightContain(byte[] item);

    // ------------------------------
    /**
     * Gets number of items have been put in the bloomfilter.
     * 
     * @return
     */
    public long size();

    /**
     * Gets the expected false-positive-probability value.
     * 
     * @return
     */
    public double expectedFpp();

    /**
     * Name of this bloomfilter.
     * 
     * @return
     */
    public String name();
}
