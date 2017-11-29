package edu.nwnu.ququzone.extractor.service;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import edu.nwnu.ququzone.extractor.result.FailureResult;
import edu.nwnu.ququzone.extractor.result.Result;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.Proxy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.net.InetSocketAddress;


/**
 * abstract extractor.
 *
 * @author Yang XuePing
 */
public abstract class AbstractExtractor implements Extractor {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractExtractor.class);

    protected OkHttpClient client;

    protected AbstractExtractor() {
        this.client = new OkHttpClient();
        this.client.setConnectTimeout(20, TimeUnit.SECONDS);
        this.client.setReadTimeout(20, TimeUnit.SECONDS);
        // this.client.getHostConfiguration().setProxy("192.168.255.51", 3128);
        // this.client.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.255.51", 3128)));
    }

    @Override
    public Result extract(String url) {
        try {
            Document doc = getDocument(url);
            if (doc == null) {
                return new FailureResult(String.format("fetch %s document error.", url));
            }
            return parse(doc);
        } catch (ParseException e) {
            LOG.error(String.format("parse %s document exception.", url), e);
            return new FailureResult(e.getMessage());
        } catch (Exception e) {
            LOG.error(String.format("fetch %s document exception.", url), e);
            return new FailureResult(String.format("fetch %s document exception.", url));
        }
    }

    public Result extractPost(String text){
        String url = "http://www.baidu.com";
        try {
            Document doc = getDocumentByText(text, url);
            if (doc == null) {
                return new FailureResult(String.format("fetch %s document error.", url));
            }
            return parse(doc);
        } catch (ParseException e) {
            LOG.error(String.format("parse %s document exception.", url), e);
            return new FailureResult(e.getMessage());
        } catch (Exception e) {
            LOG.error(String.format("fetch %s document exception.", url), e);
            return new FailureResult(String.format("fetch %s document exception.", url));
        }
    }

    protected Document getDocumentByText(String text, String url){
        try {
            byte[] data = text.getBytes();
            String encoding = detectEncoding(data);
            return Jsoup.parse(new String(data, encoding), url);
        } catch (IOException e) {
            LOG.error("fetch document error:" + url, e);
        }
        return null;
    }

    protected abstract Result parse(Document doc);

// 获取编码后的HTML页面
    protected Document getDocument(String url) {
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                byte[] data = response.body().bytes();
                String encoding = detectEncoding(data);
                return Jsoup.parse(new String(data, encoding), url);
            } else {
                throw new RuntimeException(String.format("get %s document error", url));
            }
        } catch (IOException e) {
            LOG.error("fetch document error:" + url, e);
        }
        return null;
    }

//    protected Document getDocumentFormFile(String filename){
//
//    }

    protected String detectEncoding(byte[] data) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(data, 0, data.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        return encoding;
    }
}
