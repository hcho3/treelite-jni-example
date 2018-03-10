import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleServer {
  private static final String HOSTNAME = "localhost";
  private static final int PORT = 8080;
  private static final int BACKLOG = 1;

  private static final String HEADER_ALLOW = "Allow";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";

  private static final Charset CHARSET = StandardCharsets.UTF_8;

  private static final int STATUS_OK = 200;
  private static final int STATUS_BAD_REQUEST = 400;
  private static final int STATUS_METHOD_NOT_ALLOWED = 405;

  private static final int NO_RESPONSE_LENGTH = -1;

  private static final String METHOD_POST = "POST";
  private static final String METHOD_OPTIONS = "OPTIONS";
  private static final String ALLOWED_METHODS
                                          = METHOD_POST + "," + METHOD_OPTIONS;

  public static void main(String[] args) throws IOException {
    HttpServer server 
          = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
    server.createContext("/func1", he -> {
      try {
        Headers headers = he.getResponseHeaders();
        String method = he.getRequestMethod().toUpperCase();
        switch (method) {
          case METHOD_POST:
            String query;
            InputStream in = he.getRequestBody();
            try {
              ByteArrayOutputStream out = new ByteArrayOutputStream();
              byte buf[] = new byte[40960];
              for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                out.write(buf, 0, n);
              }
              query = new String(out.toByteArray(), CHARSET);
            } finally {
              in.close();
            }
            try {
              float result = PredictWrapper.predict(query);
              // send response
              String responseBody = "{\"result\":" + result + "}";
              headers.set(HEADER_CONTENT_TYPE,
                          String.format("application/json; charset=%s",
                                        CHARSET));
              byte[] rawResponseBody = responseBody.getBytes(CHARSET);
              he.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
              he.getResponseBody().write(rawResponseBody);
            } catch (IllegalArgumentException e) {
              System.out.println("Bad request: " + e.getMessage());
              headers.set(HEADER_CONTENT_TYPE,
                          String.format("application/json; charset=%s",
                                        CHARSET));
              String responseBody = "{\"error\":\"" + e.getMessage() + "\"}";
              byte[] rawResponseBody = responseBody.getBytes(CHARSET);
              he.sendResponseHeaders(STATUS_BAD_REQUEST, rawResponseBody.length);
              he.getResponseBody().write(rawResponseBody);
            }

            break;
          case METHOD_OPTIONS:
            headers.set(HEADER_ALLOW, ALLOWED_METHODS);
            he.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
            break;
          default:
            headers.set(HEADER_ALLOW, ALLOWED_METHODS);
            he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
            break;
        }
      } finally {
        he.close();
      }
    });
    server.start();
  }

  private static String decode(String encoded_string) {
    try {
      return URLDecoder.decode(encoded_string, CHARSET.name());
    } catch (final UnsupportedEncodingException ex) {
      throw new InternalError(ex);
    }
  }
}
