package thrift;

import globals.Registry;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;

import play.Logger;
import util.Constants;
import api.BloomApi;

import com.github.btnguyen2k.bloomserver.thrift.TBloomResponse;
import com.github.btnguyen2k.bloomserver.thrift.TBloomService;
import com.github.ddth.plommon.utils.PlayAppUtils;

public class TBloomServiceImpl implements TBloomService.Iface {

    public final static TBloomServiceImpl instance = new TBloomServiceImpl();

    /**
     * {@inheritDoc}
     */
    @Override
    public void ping() throws TException {
        Registry.incConcurrency();
        try {
        } finally {
            Registry.decConcurrency();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ping2() throws TException {
        Registry.incConcurrency();
        try {
            return true;
        } finally {
            Registry.decConcurrency();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TBloomResponse put(String _bloomName, String _item) throws TException {
        BloomApi bloomApi = Registry.getBloomApi();
        try {
            int result = bloomApi.put(_bloomName, _item != null ? _item.getBytes(Constants.UTF8)
                    : Constants.EMPTY);
            switch (result) {
            case Constants.API_RESULT_BLOOM_NOTFOUND:
                return doResponse(404, false, "Bloom filter [" + _bloomName + "] does not exist!");
            case Constants.API_RESULT_INVALID_PARAM:
                return doResponse(400, false, "Invalid item: " + _item);
            case Constants.API_RESULT_FALSE:
                return doResponse(200, false, "Successful");
            case Constants.API_RESULT_TRUE:
                return doResponse(200, true, "Successful");
            }
            return doResponse(500, false, "Error");
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, false, logMsg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TBloomResponse mightContain(String _bloomName, String _item) throws TException {
        BloomApi bloomApi = Registry.getBloomApi();
        try {
            int result = bloomApi.mightContain(_bloomName,
                    _item != null ? _item.getBytes(Constants.UTF8) : Constants.EMPTY);
            switch (result) {
            case Constants.API_RESULT_BLOOM_NOTFOUND:
                return doResponse(404, false, "Bloom filter [" + _bloomName + "] does not exist!");
            case Constants.API_RESULT_INVALID_PARAM:
                return doResponse(400, false, "Invalid item: " + _item);
            case Constants.API_RESULT_FALSE:
                return doResponse(200, false, "Successful");
            case Constants.API_RESULT_TRUE:
                return doResponse(200, true, "Successful");
            }
            return doResponse(500, false, "Error");
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, false, logMsg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TBloomResponse initBloom(String _secret, String _bloomName, long _numItems,
            double _expectedFpp, boolean _force, boolean _counting, boolean _scaling) {
        if (_numItems < 1) {
            _numItems = Constants.DEFAULT_EXPECTED_NUM_ITEMS;
        }
        if (_expectedFpp <= 0 || _expectedFpp >= 1) {
            _expectedFpp = Constants.DEFAULT_EXPECTED_FPP;
        }
        // counting & scaling are not currently supported

        // authorize the request
        String mySecret = PlayAppUtils.appConfigString("bloom.secret");
        if (StringUtils.isBlank(mySecret) || !StringUtils.equals(_secret, mySecret)) {
            return doResponse(403, false, "Unauthorized request!");
        }

        BloomApi bloomApi = Registry.getBloomApi();
        try {
            boolean result = bloomApi.initBloomFilter(_bloomName, _force, _numItems, _expectedFpp);
            Map<String, Object> resultData = new HashMap<String, Object>();
            resultData.put("num_items", _numItems);
            resultData.put("fpp", _expectedFpp);
            return doResponse(200, result, resultData.toString());
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, false, logMsg);
        }
    }

    private static TBloomResponse doResponse(int status, boolean value, String message) {
        TBloomResponse response = new TBloomResponse(status, value, message);
        return response;
    }
}
