package com.agritech.platform.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NwdpWindService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${app.nwdp.base-url:https://www.nwdp.nwic.gov.in/api/3/action/datastore_search}")
    private String baseUrl;

    @Value("${app.nwdp.resource-id:a2a13ede-4a16-4484-a66d-e7953cbd57bb}")
    private String resourceId;

    public NwdpWindService(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> fetchWindData(String state, String district, String agency, String format) {
        Map<String, String> filters = new LinkedHashMap<>();
        if (state != null && !state.isBlank()) {
            filters.put("State", state.trim());
        }
        if (district != null && !district.isBlank()) {
            filters.put("District", district.trim());
        }
        if (agency != null && !agency.isBlank()) {
            filters.put("Agency", agency.trim());
        }

        String filtersJson;
        try {
            filtersJson = objectMapper.writeValueAsString(filters);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to encode NWDP filters", e);
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("resource_id", resourceId)
                .queryParam("filters", filtersJson)
                .queryParam("limit", 100)
                .build()
                .toUri();

        Map<String, Object> response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return Map.of("result", Map.of("records", List.of()));
        }

        Map<String, Object> result = (Map<String, Object>) response.getOrDefault("result", Map.of());
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.getOrDefault("records", List.of());

        response.put("format", format != null ? format : "JSON");
        result.put("records", records.stream().map(this::normalizeWindRecord).toList());
        return response;
    }

    private Map<String, Object> normalizeWindRecord(Map<String, Object> record) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.putAll(record);

        Object value = record.get("Telemetry Hourly Wind Speed (Km/Hr)");
        double windSpeed = 0d;
        if (value != null) {
            String text = value.toString().trim();
            if (!text.isEmpty() && !"-".equals(text)) {
                try {
                    windSpeed = Double.parseDouble(text);
                } catch (NumberFormatException ignored) {
                    // keep zero if the API sends a non-numeric value
                }
            }
        }

        normalized.put("windSpeed", windSpeed);
        normalized.put("recordedAt", record.getOrDefault("Data Acquisition Time", ""));
        return normalized;
    }
}
