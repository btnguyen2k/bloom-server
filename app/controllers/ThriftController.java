package controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

import play.mvc.Controller;
import play.mvc.Http.RawBuffer;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import thrift.TBloomServiceImpl;
import util.Constants;

import com.github.btnguyen2k.bloomserver.thrift.TBloomService;

public class ThriftController extends Controller {

    static TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
    static TProcessor processor = new TBloomService.Processor<TBloomService.Iface>(
            TBloomServiceImpl.instance);

    public static Result doPost() throws Exception {
        RequestBody requestBody = request().body();
        byte[] requestContent = null;
        RawBuffer rawBuffer = requestBody.asRaw();
        if (rawBuffer != null) {
            requestContent = rawBuffer.asBytes();
        } else {
            requestContent = requestBody.asText().getBytes(Constants.UTF8);
        }

        InputStream in = new ByteArrayInputStream(requestContent);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TTransport transport = new TIOStreamTransport(in, out);
        TProtocol inProtocol = protocolFactory.getProtocol(transport);
        TProtocol outProtocol = protocolFactory.getProtocol(transport);
        processor.process(inProtocol, outProtocol);
        response().setHeader(CONTENT_TYPE, "application/x-thrift");
        return ok(out.toByteArray());
    }
}
