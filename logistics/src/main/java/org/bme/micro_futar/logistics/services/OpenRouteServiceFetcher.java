package org.bme.micro_futar.logistics.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class OpenRouteServiceFetcher {

    @Value("${open-route-service.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public double[] geocode(String address) {
        String url = UriComponentsBuilder
                .fromUriString("https://api.openrouteservice.org/geocode/search")
                .queryParam("api_key", apiKey)
                .queryParam("text", address)
                .toUriString();
        ResponseEntity<Map<String, Object>> res = restTemplate.getForEntity(url, (Class<Map<String, Object>>)(Class<?>)Map.class);
        Map<String, Object> body = res.getBody();
        if (body == null) {
            throw new IllegalStateException("No response body from geocoding service");
        }
        List<Map<String, Object>> features = (List<Map<String, Object>>) body.get("features");
        if (features == null || features.isEmpty()) {
            throw new IllegalStateException("No features found in geocoding response");
        }
        Map<String, Object> feature = features.getFirst();
        Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
        if (geometry == null) {
            throw new IllegalStateException("No geometry found in feature");
        }
        List<Double> coords = (List<Double>) geometry.get("coordinates");
        if (coords == null || coords.size() < 2) {
            throw new IllegalStateException("Invalid coordinates in geometry");
        }
        return new double[]{coords.get(0), coords.get(1)}; // [lon, lat]
    }

    @SuppressWarnings("unchecked")
    public int getClosestIndexByDuration(double[] origin, List<double[]> destinations) {
        String url = "https://api.openrouteservice.org/v2/matrix/driving-car";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        List<double[]> allLocations = new ArrayList<>();
        allLocations.add(origin);
        allLocations.addAll(destinations);

        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", allLocations);
        payload.put("sources", Collections.singletonList(0));
        payload.put("destinations", 0);
        payload.put("metrics", Collections.singletonList("duration"));
        payload.put("units", "m"); // Optional

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Map<String, Object>> res = restTemplate.postForEntity(url, entity, (Class<Map<String, Object>>)(Class<?>)Map.class);
        Map<String, Object> body = res.getBody();
        if (body == null) {
            throw new IllegalStateException("No response body from matrix service");
        }
        List<List<Double>> durationsList = (List<List<Double>>) body.get("durations");
        if (durationsList == null || durationsList.isEmpty()) {
            throw new IllegalStateException("No durations found in matrix response");
        }
        List<Double> durations = durationsList.getFirst();

        double minDuration = Collections.min(durations);
        return durations.indexOf(minDuration); // index in your destinations list
    }
}