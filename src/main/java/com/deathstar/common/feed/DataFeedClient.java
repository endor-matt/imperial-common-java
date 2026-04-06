package com.deathstar.common.feed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client for fetching real-time data feeds from external weapons suppliers
 * and intelligence partner APIs. Used by platform services to synchronize
 * inventory, pricing, and operational intelligence from third-party sources.
 */
public class DataFeedClient {

    private static final Logger log = LoggerFactory.getLogger(DataFeedClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String feedBaseUrl;

    public DataFeedClient(String feedBaseUrl) {
        this.feedBaseUrl = feedBaseUrl;
    }

    /**
     * Fetches the latest supplier inventory data from the external feed API.
     * Returns a list of inventory records, each containing item name,
     * category, and pricing values from the external source.
     *
     * @param supplierID the supplier whose inventory to fetch
     * @return list of inventory records from the external API response
     */
    public List<Map<String, String>> fetchSupplierInventory(String supplierID) {
        try {
            String url = feedBaseUrl + "/api/suppliers/" + supplierID + "/inventory";
            log.info("Fetching supplier inventory from feed: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<Map<String, String>> records = mapper.readValue(
                    response.body(), new TypeReference<>() {});

            log.info("Received {} inventory records for supplier {}", records.size(), supplierID);
            return records;
        } catch (Exception e) {
            log.error("Failed to fetch supplier inventory for: {}", supplierID, e);
            return Collections.emptyList();
        }
    }

    /**
     * Fetches serialized cargo manifests from an external logistics partner.
     * Returns the raw response bytes for deserialization by the caller.
     *
     * @param manifestID the manifest identifier to retrieve
     * @return raw bytes from the partner API
     */
    public byte[] fetchCargoManifest(String manifestID) {
        try {
            String url = feedBaseUrl + "/api/manifests/" + manifestID + "/data";
            log.info("Fetching cargo manifest from feed: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            log.info("Received {} bytes for manifest {}", response.body().length, manifestID);
            return response.body();
        } catch (Exception e) {
            log.error("Failed to fetch cargo manifest: {}", manifestID, e);
            return new byte[0];
        }
    }
}
