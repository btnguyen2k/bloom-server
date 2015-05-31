package controllers;

import globals.Registry;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.api.templates.Html;
import play.mvc.Http.RawBuffer;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import util.Constants;
import api.BloomApi;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.plommon.utils.PlayAppUtils;
import com.github.ddth.tsc.DataPoint;
import com.github.ddth.tsc.ICounter;
import com.github.ddth.tsc.ICounterFactory;

public class Application extends BaseController {

    private static Result doResponse(int status, boolean value, String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(Constants.RESPONSE_FIELD_STATUS, status);
        result.put(Constants.RESPONSE_FIELD_VALUE, value);
        result.put(Constants.RESPONSE_FIELD_MESSAGE, message);
        response().setHeader(CONTENT_TYPE, "application/json");
        response().setHeader(CONTENT_ENCODING, "utf-8");
        Registry.updateCounters(status);
        return ok(SerializationUtils.toJsonString(result));
    }

    /*
     * Handles: GET:/put/:item/:bloomName
     */
    public static Result put(final String item, final String bloomName) {
        BloomApi bloomApi = Registry.getBloomApi();
        try {
            int result = bloomApi.put(bloomName, item != null ? item.getBytes(Constants.UTF8)
                    : Constants.EMPTY);
            switch (result) {
            case Constants.API_RESULT_BLOOM_NOTFOUND:
                return doResponse(404, false, "Bloom filter [" + bloomName + "] does not exist!");
            case Constants.API_RESULT_INVALID_PARAM:
                return doResponse(400, false, "Invalid item: " + item);
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

    /*
     * Handles: GET:/mightContain/:item/:bloomname
     */
    public static Result mightContain(final String item, final String bloomName) {
        BloomApi bloomApi = Registry.getBloomApi();
        try {
            int result = bloomApi.mightContain(bloomName,
                    item != null ? item.getBytes(Constants.UTF8) : Constants.EMPTY);
            switch (result) {
            case Constants.API_RESULT_BLOOM_NOTFOUND:
                return doResponse(404, false, "Bloom filter [" + bloomName + "] does not exist!");
            case Constants.API_RESULT_INVALID_PARAM:
                return doResponse(400, false, "Invalid item: " + item);
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

    /*
     * Handles: POST:/initBloom
     */
    @SuppressWarnings("unchecked")
    public static Result initBloom() {
        RequestBody requestBody = request().body();
        String requestContent = null;
        JsonNode jsonNode = requestBody.asJson();
        if (jsonNode != null) {
            requestContent = jsonNode.toString();
        } else {
            RawBuffer rawBuffer = requestBody.asRaw();
            if (rawBuffer != null) {
                requestContent = new String(rawBuffer.asBytes(), Constants.UTF8);
            } else {
                requestContent = requestBody.asText();
            }
        }

        Map<String, Object> params = SerializationUtils.fromJsonString(requestContent, Map.class);
        String bloomName = DPathUtils.getValue(params, "bloom_name", String.class);
        Long expectedNumItems = DPathUtils.getValue(params, "num_items", Long.class);
        if (expectedNumItems == null || expectedNumItems.longValue() < 1) {
            expectedNumItems = Constants.DEFAULT_EXPECTED_NUM_ITEMS;
        }
        Double expectedFpp = DPathUtils.getValue(params, "expected_fpp", Double.class);
        if (expectedFpp == null || expectedFpp.doubleValue() <= 0 || expectedFpp.doubleValue() >= 1) {
            expectedFpp = Constants.DEFAULT_EXPECTED_FPP;
        }
        Boolean force = DPathUtils.getValue(params, "force", Boolean.class);
        if (force == null) {
            force = Boolean.FALSE;
        }
        String secret = DPathUtils.getValue(params, "secret", String.class);

        // authorize the request
        String mySecret = PlayAppUtils.appConfigString("bloom.secret");
        if (StringUtils.isBlank(mySecret) || !StringUtils.equals(secret, mySecret)) {
            return doResponse(403, false, "Unauthorized request!");
        }

        BloomApi bloomApi = Registry.getBloomApi();
        try {
            boolean result = bloomApi.initBloomFilter(bloomName, force.booleanValue(),
                    expectedNumItems.longValue(), expectedFpp.doubleValue());
            Map<String, Object> resultData = new HashMap<String, Object>();
            resultData.put("num_items", expectedNumItems.longValue());
            resultData.put("fpp", expectedFpp.doubleValue());
            return doResponse(200, result, resultData.toString());
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, false, logMsg);
        }
    }

    private static DataPoint[] buildCounterData(ICounter counter, long timestamp) {
        long last1Min = timestamp - 60 * 1000L;
        long last5Mins = timestamp - 5 * 60 * 1000L;
        long last15Mins = timestamp - 15 * 60 * 1000L;
        DataPoint[] result = new DataPoint[] {
                new DataPoint(DataPoint.Type.SUM, last1Min, 0, ICounter.STEPS_1_MIN * 1000),
                new DataPoint(DataPoint.Type.SUM, last5Mins, 0, ICounter.STEPS_5_MINS * 1000),
                new DataPoint(DataPoint.Type.SUM, last15Mins, 0, ICounter.STEPS_15_MINS * 1000) };
        if (counter == null) {
            return result;
        }

        DataPoint[] tempArr = counter.getSeries(last1Min, timestamp);
        for (DataPoint dp : tempArr) {
            result[0].add(dp);
        }
        tempArr = counter.getSeries(last5Mins, timestamp);
        for (DataPoint dp : tempArr) {
            result[1].add(dp);
        }
        tempArr = counter.getSeries(last15Mins, timestamp);
        for (DataPoint dp : tempArr) {
            result[2].add(dp);
        }
        return result;
    }

    /*
     * Handle: GET:/index
     */
    public static Result index() throws Exception {
        Map<String, DataPoint[]> statsLocal = new HashMap<String, DataPoint[]>();
        Map<String, DataPoint[]> statsGlobal = new HashMap<String, DataPoint[]>();
        Map<String, Long> countersLocal = new HashMap<String, Long>();
        Map<String, Long> countersGlobal = new HashMap<String, Long>();
        ICounterFactory localCounterFactory = Registry.getLocalCounterFactory();
        ICounterFactory globalCounterFactory = Registry.getGlobalCounterFactory();

        long timestamp = System.currentTimeMillis();
        String[] tscNames = new String[] { Registry.TSC_TOTAL, Registry.TSC_200, Registry.TSC_400,
                Registry.TSC_403, Registry.TSC_404, Registry.TSC_500 };
        for (String name : tscNames) {
            statsLocal.put(
                    name,
                    buildCounterData(
                            localCounterFactory != null ? localCounterFactory.getCounter(name)
                                    : null, timestamp));
            statsGlobal.put(
                    name,
                    buildCounterData(
                            globalCounterFactory != null ? globalCounterFactory.getCounter(name)
                                    : null, timestamp));
        }

        String[] counterNames = new String[] { Registry.COUNTER_TOTAL, Registry.COUNTER_200,
                Registry.COUNTER_400, Registry.COUNTER_403, Registry.COUNTER_404,
                Registry.COUNTER_500 };
        for (String name : counterNames) {
            ICounter counter = localCounterFactory != null ? localCounterFactory.getCounter(name)
                    : null;
            DataPoint dp = counter != null ? counter.get(0) : null;
            long value = dp != null ? dp.value() : 0;
            countersLocal.put(name, value);

            counter = globalCounterFactory != null ? globalCounterFactory.getCounter(name) : null;
            dp = counter != null ? counter.get(0) : null;
            value = dp != null ? dp.value() : 0;
            countersGlobal.put(name, value);
        }

        long[] concurrency = Registry.getConcurrency();

        Html html = render("index", concurrency, countersLocal, statsLocal, countersGlobal,
                statsGlobal);
        return ok(html);
    }
}
