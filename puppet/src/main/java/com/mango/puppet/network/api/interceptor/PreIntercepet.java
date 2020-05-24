package com.mango.puppet.network.api.interceptor;

import android.util.Log;

import com.mango.puppet.network.api.gson.JsonFormat;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;

import static okhttp3.internal.platform.Platform.INFO;

public final class PreIntercepet implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public enum Level {
        /**
         * No logs.
         */
        NONE,
        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    private interface Logger {
        void log(String message);

        /**
         * A {@link Logger} defaults output appropriate for the current platform.
         */
        Logger DEFAULT = new Logger() {
            @Override
            public void log(String message) {
                Platform.get().log(INFO, message, null);
            }
        };
    }

    public PreIntercepet() {
        this(Logger.DEFAULT);
    }

    public PreIntercepet(Logger logger) {
        this.logger = logger;
    }

    private final Logger logger;

    private volatile Level level = Level.NONE;

    /**
     * Change the level at which this interceptor logs.
     */
    public PreIntercepet setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Level level = this.level;

        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == Level.BODY;
        boolean logHeaders = logBody || level == Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        StringBuilder requestMsg = new StringBuilder();
        requestMsg.append(requestStartMessage).append("\n");
        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    requestMsg.append("Content-Type:").append(requestBody.contentType()).append("\n");

                }
                if (requestBody.contentLength() != -1) {
                    requestMsg.append("Content-Length:").append(requestBody.contentLength()).append("\n");
                }
            }

            Headers headers = request.headers();
            requestMsg.append("┌───────────────────────────────────────────────────REQUEST_HEADERS─────────────────────────────────────────────────────────────\n");
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    requestMsg.append(name + ": ").append(headers.value(i)).append("\n");
                }
            }
            requestMsg.append("└───────────────────────────────────────────────────REQUEST_HEADERS─────────────────────────────────────────────────────────────\n");

            if (!logBody || !hasRequestBody) {
                requestMsg.append("--------------------------> END ").append(request.method()).append("\n");
            } else if (bodyEncoded(request.headers())) {
                requestMsg.append("--------------------------> END ").append(request.method()).append(" (encoded body omitted)").append("\n");

            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (isPlaintext(buffer)) {
                    String req = new JsonFormat().formatJson(buffer.readString(charset));
                    requestMsg.append(req).append("\n");
                    requestMsg.append("--------------------------> END ").append(request.method()).append(" (").append(requestBody.contentLength()).append("-byte body)").append("\n");

                } else {
                    requestMsg.append("--------------------------> END ").append(request.method()).append(" (binary ").append(requestBody.contentLength()).append("-byte body omitted)").append("\n");

                }
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            requestMsg.append("<-------------------------- HTTP FAILED: ").append(e).append("\n");
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";

        requestMsg.append("<-------------------------- ").append(response.code()).append(' ').append(response.message()).append(' ').append(response.request().url()).append(" (").append(tookMs).append("ms").append(!logHeaders ? ", "
                + bodySize + " body" : "").append(')').append("\n");
        if (logHeaders) {
            requestMsg.append("┌───────────────────────────────────────────────────RESPONSE_HEADERS─────────────────────────────────────────────────────────────\n");
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                requestMsg.append(headers.name(i)).append(": ").append(headers.value(i)).append("\n");
            }
            requestMsg.append("└───────────────────────────────────────────────────RESPONSE_HEADERS─────────────────────────────────────────────────────────────\n");


            if (!logBody || !HttpHeaders.hasBody(response)) {
                requestMsg.append("<-------------------------- END HTTP").append("\n");
            } else if (bodyEncoded(response.headers())) {
                requestMsg.append("<-------------------------- END HTTP (encoded body omitted)").append("\n");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
                    requestMsg.append("<-------------------------- END HTTP (binary ").append(buffer.size()).append("-byte body omitted)").append("\n");
                    return response;
                }

                if (contentLength != 0) {
                    requestMsg.append("┌───────────────────────────────────────────────────RESPONSE_BODY─────────────────────────────────────────────────────────────\n");
                    String res = new JsonFormat().formatJson(buffer.clone().readString(charset));
                    requestMsg.append(res).append("\n");
                    requestMsg.append("└───────────────────────────────────────────────────RESPONSE_BODY─────────────────────────────────────────────────────────────\n");
                }
                requestMsg.append("<-------------------------- END HTTP (").append(buffer.size()).append("-byte body)").append("\n");

            }
        }
        Log.i("PreIntercept", requestMsg.toString());
        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }

    /**
     * set contentType in headers
     *
     * @param response
     * @throws IOException
     */
    private void setHeaderContentType(Response response) throws IOException {

        // build new headers
        Headers headers = response.headers();
        Headers.Builder builder = headers.newBuilder();
        builder.removeAll("Content-Type");
        builder.add("Content-Type", "application/json;charset=UTF-8");
        headers = builder.build();
        // setting headers using reflect
        Class _response = Response.class;
        try {
            Field field = _response.getDeclaredField("headers");
            field.setAccessible(true);
            field.set(response, headers);
        } catch (NoSuchFieldException e) {
            throw new IOException("use reflect to setting header occurred an error", e);
        } catch (IllegalAccessException e) {
            throw new IOException("use reflect to setting header occurred an error", e);
        }
    }
}
