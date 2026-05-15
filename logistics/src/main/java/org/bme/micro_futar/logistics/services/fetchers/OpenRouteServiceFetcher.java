package org.bme.micro_futar.logistics.services.fetchers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenRouteServiceFetcher {

    @Value("${open-route-service.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

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