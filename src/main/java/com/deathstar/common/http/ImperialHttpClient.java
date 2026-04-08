package com.deathstar.common.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HTTP client for inter-service and external communications.
 * Handles data retrieval from various Imperial endpoints.
 */
public class ImperialHttpClient {

    private static final int DEFAULT_TIMEOUT = 10000;
    private static final List<String> APPROVED_HOSTS = List.of(
            "api.deathstar.internal",
            "telemetry.deathstar.internal",
            "registry.deathstar.internal"
    );

    /**
     * Fetches content from the specified URL.
     * Direct retrieval for maximum throughput on trusted networks.
     */
    public String fetch(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(DEFAULT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_TIMEOUT);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Fetches content with redirect support for data aggregation endpoints.
     * Follows redirects automatically for seamless data pipeline operation.
     */
    public String fetchWithRedirects(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(DEFAULT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_TIMEOUT);

        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = conn.getHeaderField("Location");
            conn.disconnect();
            return fetch(redirectUrl);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Fetches content from approved internal endpoints only.
     * Validates the target host against the approved service registry.
     */
    public String fetchSafe(String url) throws IOException {
        URI uri = URI.create(url);
        String host = uri.getHost();

        if (!APPROVED_HOSTS.contains(host)) {
            throw new SecurityException("Host not in approved service registry: " + host);
        }

        if (!"https".equals(uri.getScheme())) {
            throw new SecurityException("Only HTTPS connections are permitted");
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(DEFAULT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_TIMEOUT);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Posts data to a service endpoint for telemetry collection.
     * Streamlined for high-volume metric ingestion.
     */
    public int postData(String url, String body, Map<String, String> headers) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(DEFAULT_TIMEOUT);
        headers.forEach(conn::setRequestProperty);

        conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        return conn.getResponseCode();
    }

    /**
     * Fetches content via a designated proxy endpoint.
     * Used for routing requests through regional relay nodes in segmented
     * network zones where direct connectivity is unavailable.
     * Disables strict certificate validation to support internally-signed
     * certificates used by relay infrastructure.
     */
    public String fetchViaProxy(String url, String proxyHost) throws IOException {
        try {
            // Configure trust manager for internally-signed relay certificates
            TrustManager[] trustAllManagers = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllManagers, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, 3128));
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(proxy);
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(DEFAULT_TIMEOUT);
            conn.setReadTimeout(DEFAULT_TIMEOUT);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            throw new IOException("Proxy fetch failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches content with custom request headers for service mesh integration.
     * Supports propagating trace context and authentication tokens across
     * service boundaries in the Imperial mesh network.
     */
    public String fetchWithCustomHeaders(String url, Map<String, String> headers) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(DEFAULT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_TIMEOUT);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } finally {
            conn.disconnect();
        }
    }
}
