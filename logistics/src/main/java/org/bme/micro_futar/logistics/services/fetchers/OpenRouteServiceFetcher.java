package org.bme.micro_futar.logistics.services.fetchers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class OpenRouteServiceFetcher {

    @Value("${open-route-service.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public double[] geocode(String address) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("https://api.openrouteservice.org/geocode/search")
                .queryParam("api_key", apiKey)
                .queryParam("text", address);
        URI uri = builder.build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Spring-RestTemplate");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );
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
        if (destinations.size() == 1) {
            return 0;
        }
        String url = "https://api.openrouteservice.org/v2/matrix/driving-car";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        List<double[]> allLocations = new ArrayList<>();
        allLocations.add(origin);
        allLocations.addAll(destinations);
        List<Integer> destinationsIndexList = new ArrayList<>();
        for (int i = 1; i < allLocations.size(); i++) {
            destinationsIndexList.add(i);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("locations", allLocations);
        payload.put("sources", Collections.singletonList(0));
        payload.put("destinations", destinationsIndexList);
        payload.put("metrics", Collections.singletonList("duration"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Map<String, Object>> res = restTemplate.postForEntity(url, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);
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