package at.ac.ait.sac;

import android.webkit.MimeTypeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


/**
 * Provides utility methods for communicating with a server.
 */
public class NetworkUtil {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkUtil.class);

    private final static int HTTP_TIMEOUT_SECONDS = 30;
    private static final HttpLoggingInterceptor.Level LOGGING_LEVEL = HttpLoggingInterceptor.Level.BODY; //BASIC; //BODY;


    public static OkHttpClient createClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder()
                .connectTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .followRedirects(true).retryOnConnectionFailure(true);
        HttpLoggingInterceptor httpLogging = new HttpLoggingInterceptor();
        httpLogging.setLevel(LOGGING_LEVEL);
        clientBuilder.addInterceptor(httpLogging);
        /*
        we are using the default host name verifier as recommended at
        https://developer.android.com/training/articles/security-ssl.html

        -> VPN based attacks cannot be circumvented, since the hostname would be still the expected one
         */
        clientBuilder.hostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        return clientBuilder.build();
    }


    public final static class HttpHeaders {
        static final String ACCEPT = "Accept";
        static final String ACCEPT_LANGUAGE = "Accept-Language";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String LOCATION = "Location";
        public static final String USER_AGENT = "User-Agent";

        public final static String AUTH_SCHEMA_HEADER = "X-Authentication-Schema";
        public final static String AUTH_SCHEMA_HEADER_VALUE = "Mobile";

        /**
         * The symbiote access token after login
         */
        public final static String X_AUTH_TOKEN = "X-AUTH-Token";


        public final static class Header {
            private final String name;
            private final String value;

            Header(String name, String value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public String getValue() {
                return value;
            }


        }


        final static class ACCEPT_JSON {
            final static Header HEADER = new Header(HttpHeaders.ACCEPT, ContentTypes.JSON);
        }

        public final static Collection<Header> JSON_GET_HEADERS = Arrays.asList(new Header[]{
                ACCEPT_JSON.HEADER
        });

        public static Collection<Header> listDefaultHeaders() {
            Collection<Header> result = new ArrayList<>();
            result.add(getAcceptLanguageFromLocale());
            return result;
        }

        private static Header getAcceptLanguageFromLocale() {
            StringBuilder sb = new StringBuilder(Locale.getDefault().getLanguage());
            String country = Locale.getDefault().getCountry();
            if (country != null && country.length() > 0) {
                sb.append("-");
                sb.append(country);
            }
            String value = sb.toString();
            LOG.debug("Setting " + HttpHeaders.ACCEPT_LANGUAGE + " to " + value);
            return new Header(HttpHeaders.ACCEPT_LANGUAGE, value);
        }
    }

    public final static class ContentTypes {
        public static final String JSON = "application/json";
    }

    private final static class ContentEncoding {
        public final static String UTF8 = "UTF-8";
    }

    public static String ensureTrailingSlash(String string) {
        if (string != null) {
            return string.endsWith("/") ? string : string + "/";
        } else {
            return null;
        }

    }

}
