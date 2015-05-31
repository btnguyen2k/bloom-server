package bloomfilter.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.github.ddth.plommon.utils.AkkaUtils;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;

/**
 * Guava-implementation of {@link IBloomFilter}.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class GuavaBloomFilter extends BaseBloomFilter {

    private String persistentPath;
    private boolean loadFromFile = false;
    private AtomicLong size = new AtomicLong(0);

    private AtomicLong counter = new AtomicLong(0);
    private Cancellable cWorker;
    private BloomFilter<byte[]> bf;

    /**
     * Constructs a new {@link GuavaBloomFilter} with default settings.
     */
    public GuavaBloomFilter() {
    }

    /*----------------------------------------------------------------------*/
    public GuavaBloomFilter clear() {
        if (!StringUtils.isBlank(persistentPath)) {
            FileUtils.deleteQuietly(new File(persistentPath));
        }
        bf = createBloomFilter();
        return this;
    }

    private BloomFilter<byte[]> createBloomFilter() {
        long expectedNumItems = expectedNumItems();
        double expectedFpp = expectedFpp();
        Funnel<byte[]> funnel = Funnels.byteArrayFunnel();
        return BloomFilter.create(funnel, (int) expectedNumItems, expectedFpp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuavaBloomFilter init() {
        super.init();

        if (!StringUtils.isBlank(persistentPath)) {
            bf = loadFromFile(persistentPath);
        }
        if (bf == null) {
            loadFromFile = false;
            bf = createBloomFilter();
        } else {
            loadFromFile = true;
            expectedFpp(bf.expectedFpp());
            expectedNumItems(0);
        }

        if (!StringUtils.isBlank(persistentPath)) {
            ActorRef workerRef = AkkaUtils.actorSystem().actorOf(Props.create(Worker.class, this));
            cWorker = AkkaUtils.scheduler().schedule(Duration.create(10, TimeUnit.SECONDS),
                    Duration.create(1, TimeUnit.SECONDS), workerRef, "",
                    AkkaUtils.actorSystem().dispatcher(), null);
        }

        return this;
    }

    private BloomFilter<byte[]> loadFromFile(String path) {
        File file = new File(path);
        if (!file.isFile() || !file.canRead()) {
            Logger.warn("Invalid file [" + file.getAbsolutePath() + "] or file is not readable!");
            return null;
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            try {
                return BloomFilter.readFrom(bis, Funnels.byteArrayFunnel());
            } finally {
                IOUtils.closeQuietly(bis);
            }
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return null;
        }
    }

    synchronized private boolean writeToFile(BloomFilter<?> bf, String path) {
        File file = new File(path);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            try {
                bf.writeTo(bos);
                counter.set(0);
                return true;
            } finally {
                IOUtils.closeQuietly(bos);
            }
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        try {
            if (cWorker != null) {
                cWorker.cancel();
            }
        } catch (Exception e) {
            Logger.warn(e.getMessage(), e);
        }

        super.destroy();
    }

    /*----------------------------------------------------------------------*/
    public static class Worker extends UntypedActor {
        private File file;
        private boolean doNotWriteToFile = false;
        private GuavaBloomFilter guavaBloomFilter;

        public Worker(GuavaBloomFilter guavaBloomFilter) {
            this.guavaBloomFilter = guavaBloomFilter;
        }

        private void init() {
            if (StringUtils.isBlank(guavaBloomFilter.persistentPath)) {
                doNotWriteToFile = true;
            } else {
                doNotWriteToFile = false;
                file = new File(guavaBloomFilter.persistentPath);
            }
        }

        public void onReceive(Object msg) {
            if (doNotWriteToFile) {
                return;
            }

            if (file == null) {
                synchronized (Worker.class) {
                    init();
                }
            }

            if (guavaBloomFilter.counter.get() > 0) {
                guavaBloomFilter.writeToFile(guavaBloomFilter.bf, file.getAbsolutePath());
            }
        }
    }

    /*----------------------------------------------------------------------*/
    /**
     * Path to file that stores persistent data of this bloomfilter.
     * 
     * @return
     */
    public String persistentPath() {
        return persistentPath;
    }

    /**
     * Sets path to file that stores persistent data of this bloomfilter.
     * 
     * @param persistentPath
     * @return
     */
    public GuavaBloomFilter persistentPath(String persistentPath) {
        this.persistentPath = persistentPath;
        return this;
    }

    /**
     * Alias of {@link #persistentPath()}.
     * 
     * @return
     */
    public String getPersistentPath() {
        return persistentPath;
    }

    /**
     * Alias of {@link #persistentPath(String)}.
     * 
     * @param persistentPath
     * @return
     */
    public GuavaBloomFilter setPersistentPath(String persistentPath) {
        this.persistentPath = persistentPath;
        return this;
    }

    /*----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean put(byte[] item) {
        if (bf.put(item)) {
            size.incrementAndGet();
            counter.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mightContain(byte[] item) {
        return bf.mightContain(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size() {
        return loadFromFile ? -1 : size.longValue();
    }
}
