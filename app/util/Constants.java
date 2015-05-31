package util;

import java.nio.charset.Charset;

public class Constants {

    public static final Charset UTF8 = Charset.forName("UTF-8");
    public static final byte[] EMPTY = new byte[0];

    public static final String RESPONSE_FIELD_STATUS = "s";
    public static final String RESPONSE_FIELD_VALUE = "v";
    public static final String RESPONSE_FIELD_MESSAGE = "m";

    public static final int API_RESULT_TRUE = 1;
    public static final int API_RESULT_FALSE = 0;
    public static final int API_RESULT_INVALID_PARAM = 400;
    public static final int API_RESULT_BLOOM_NOTFOUND = 404;

    public static final long DEFAULT_EXPECTED_NUM_ITEMS = 1000000;
    public static final double DEFAULT_EXPECTED_FPP = 1E-6;
}
