package api;

import globals.Registry;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Play;
import util.Constants;
import bloomfilter.BloomSpec;
import bloomfilter.engine.BaseBloomFilter;
import bloomfilter.engine.GuavaBloomFilter;
import bloomfilter.engine.IBloomFilter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;

/**
 * Engine to generate IDs.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class BloomApi {

    private String storageBasePath;
    private File storageBaseDir;
    private long defaultExpectedNumItems = 1000000;
    private double defaultExpectedFpp = 1E-6;

    public String storageBasePath() {
        return storageBasePath;
    }

    public String getStorageBasePath() {
        return storageBasePath;
    }

    public BloomApi storageBasePath(String path) {
        storageBasePath = path;
        return this;
    }

    public BloomApi setStorageBasePath(String path) {
        storageBasePath = path;
        return this;
    }

    /**
     * Init method.
     * 
     * @return
     */
    public BloomApi init() {
        // init storage path
        if (!StringUtils.isBlank(storageBasePath)) {
            if (storageBasePath.startsWith("/")) {
                storageBaseDir = new File(storageBasePath);
            } else {
                File dir = Play.application().path();
                storageBaseDir = new File(dir, storageBasePath);
            }
        }
        if (storageBaseDir != null) {
            try {
                if (!storageBaseDir.isDirectory()) {
                    storageBaseDir.mkdirs();
                }
            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
                storageBaseDir = null;
            }
        }

        // init caches
        long CACHE_DURATION_SECONDS = 24 * 3600; // 1 day

        cacheSpec = CacheBuilder.newBuilder()
                .expireAfterAccess(CACHE_DURATION_SECONDS, TimeUnit.SECONDS).build();
        long MAX_MEM = Runtime.getRuntime().maxMemory();
        cacheBloom = CacheBuilder.newBuilder().maximumWeight((long) (MAX_MEM * 0.9))
                .weigher(new Weigher<String, BaseBloomFilter>() {
                    @Override
                    public int weigh(String bloomName, BaseBloomFilter bloomFilter) {
                        return (int) (bloomFilter.size() / 8);
                    }
                }).expireAfterAccess(CACHE_DURATION_SECONDS, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, BaseBloomFilter>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, BaseBloomFilter> item) {
                        item.getValue().destroy();
                    }
                }).build();

        // init "default" bloom filter
        initBloomFilter("default", false, defaultExpectedNumItems, defaultExpectedFpp);

        return this;
    }

    /**
     * Destroy method.
     */
    public void destroy() {
        if (cacheSpec != null) {
            try {
                cacheSpec.invalidateAll();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            } finally {
                cacheSpec = null;
            }
        }

        if (cacheBloom != null) {
            try {
                cacheBloom.invalidateAll();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            } finally {
                cacheBloom = null;
            }
        }
    }

    /*----------------------------------------------------------------------*/

    private static String normalizeBloomName(final String bloomName) {
        if (StringUtils.isBlank(bloomName)) {
            return "default";
        } else {
            return bloomName.trim().toLowerCase();
        }
    }

    /*----------------------------------------------------------------------*/
    private Cache<String, BloomSpec> cacheSpec = null;
    private Cache<String, BaseBloomFilter> cacheBloom = null;

    synchronized private boolean initBloomSpec(String bloomName, BloomSpec bloomSpec, boolean force) {
        BloomSpec oldSpec = cacheSpec.getIfPresent(bloomName);
        if (oldSpec == null || force) {
            cacheSpec.put(bloomName, bloomSpec);
            return true;
        }
        return false;
    }

    private BaseBloomFilter createBloomFilter(String bloomName, BloomSpec bloomSpec,
            boolean forceClearOldData) {
        BaseBloomFilter bloomFilter = new GuavaBloomFilter().name(bloomName)
                .expectedFpp(bloomSpec.expectedFpp).expectedNumItems(bloomSpec.expectedNumItems);
        if (storageBaseDir != null) {
            File storageFile = new File(storageBaseDir, bloomName + ".guava");
            ((GuavaBloomFilter) bloomFilter).persistentPath(storageFile.getAbsolutePath());
        }
        if (forceClearOldData) {
            ((GuavaBloomFilter) bloomFilter).clear();
        }
        bloomFilter.init();
        return bloomFilter;
    }

    private IBloomFilter getBloomFilter(String _bloomName, final boolean forceClearOldData) {
        final String bloomName = normalizeBloomName(_bloomName);
        IBloomFilter bloomFilter;
        try {
            bloomFilter = cacheBloom.get(bloomName, new Callable<BaseBloomFilter>() {
                @Override
                public BaseBloomFilter call() throws Exception {
                    BloomSpec spec = cacheSpec.getIfPresent(bloomName);
                    if (spec == null) {
                        return null;
                    }
                    return createBloomFilter(bloomName, spec, forceClearOldData);
                }
            });
        } catch (Exception e) {
            bloomFilter = null;
        }
        return bloomFilter;
    }

    /*--------------------------------------------------*/
    /**
     * Creates and initializes a new bloom filter.
     * 
     * @param bloomName
     * @param force
     * @param expectedNumItems
     * @param expectedFpp
     * @return {@code true} if new bloom filter has been successfully created
     *         and initialized
     */
    public boolean initBloomFilter(String _bloomName, boolean force, long expectedNumItems,
            double expectedFpp) {
        Registry.incConcurrency();
        try {
            String bloomName = normalizeBloomName(_bloomName);
            BloomSpec bloomSpec = new BloomSpec();
            bloomSpec.expectedNumItems = expectedNumItems;
            bloomSpec.expectedFpp = expectedFpp;
            if (initBloomSpec(bloomName, bloomSpec, force)) {
                if (force) {
                    cacheBloom.invalidate(bloomName);
                    getBloomFilter(_bloomName, force);
                }
                return true;
            }
            return false;
        } finally {
            Registry.decConcurrency();
        }
    }

    /**
     * Puts an item to a specified bloom filter.
     * 
     * @param bloomName
     * @param item
     * @return
     */
    public int put(String bloomName, byte[] item) {
        Registry.incConcurrency();
        try {
            if (item != null) {
                IBloomFilter bloomFilter = getBloomFilter(bloomName, false);
                return bloomFilter != null ? (bloomFilter.put(item) ? Constants.API_RESULT_TRUE
                        : Constants.API_RESULT_FALSE) : Constants.API_RESULT_BLOOM_NOTFOUND;
            }
            return Constants.API_RESULT_INVALID_PARAM;
        } finally {
            Registry.decConcurrency();
        }
    }

    /**
     * Returns {@code true} if the item might have been put in a specified bloom
     * filter.
     * 
     * @param bloomName
     * @param item
     * @return
     */
    public int mightContain(String bloomName, byte[] item) {
        Registry.incConcurrency();
        try {
            if (item != null) {
                IBloomFilter bloomFilter = getBloomFilter(bloomName, false);
                return bloomFilter != null ? (bloomFilter.mightContain(item) ? Constants.API_RESULT_TRUE
                        : Constants.API_RESULT_FALSE)
                        : Constants.API_RESULT_BLOOM_NOTFOUND;
            }
            return Constants.API_RESULT_INVALID_PARAM;
        } finally {
            Registry.decConcurrency();
        }
    }
}
