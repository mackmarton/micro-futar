package org.bme.micro_futar.logistics.services.geocoding;

import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.exceptions.GeocodingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * OpenRouteService geocoding: {@code GET /geocode/search?text=&boundary.country=&layers=locality&size=1}.
 * The answer is GeoJSON: {@code features[0].geometry.coordinates = [lon, lat]} is the centre and
 * {@code features[0].bbox = [minLon, minLat, maxLon, maxLat]} the bounding box (absent for point-only results).
 */
@Slf4j
@Service
public class OrsGeocodingClient {

    static final String SEARCH_URL = "https://api.openrouteservice.org/geocode/search";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String searchUrl;

    public OrsGeocodingClient(RestTemplate orsRestTemplate,
                              @Value("${open-route-service.api-key}") String apiKey,
                              @Value("${open-route-service.geocode-url:" + SEARCH_URL + "}") String searchUrl) {
        this.restTemplate = orsRestTemplate;
        this.apiKey = apiKey;
        this.searchUrl = searchUrl;
    }

    /**
     * @param isoCode ISO 3166-1 alpha-2 code of the country the city must be in
     * @return the best settlement match, empty when there is none or it lies in another country
     * @throws GeocodingException when the service is unreachable or answers with an error
     */
    public Optional<GeocodeResult> geocode(String cityName, String isoCode) {
        URI uri = UriComponentsBuilder.fromUriString(searchUrl)
                .queryParam("text", "{text}")
                .queryParam("boundary.country", "{country}")
                .queryParam("layers", "locality")
                .queryParam("size", 1)
                .encode()
                .buildAndExpand(cityName, isoCode.toUpperCase(Locale.ROOT))
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        try {
            Map<String, Object> body = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();
            return parse(body, isoCode);
        } catch (RestClientException e) {
            throw new GeocodingException("OpenRouteService geocoding failed for '" + cityName + "'", e);
        }
    }

    @SuppressWarnings("unchecked")
    static Optional<GeocodeResult> parse(Map<String, Object> body, String isoCode) {
        if (body == null || !(body.get("features") instanceof List<?> features) || features.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> feature = (Map<String, Object>) features.getFirst();

        Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
        if (properties != null && properties.get("country_a") instanceof String countryA
                && !countryA.equalsIgnoreCase(toAlpha3(isoCode))) {
            log.info("Geocoding hit is in country {} but {} was expected, ignoring", countryA, isoCode);
            return Optional.empty();
        }

        Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
        if (geometry == null || !(geometry.get("coordinates") instanceof List<?> coordinates) || coordinates.size() < 2) {
            return Optional.empty();
        }
        double longitude = ((Number) coordinates.get(0)).doubleValue();
        double latitude = ((Number) coordinates.get(1)).doubleValue();

        if (feature.get("bbox") instanceof List<?> bbox && bbox.size() >= 4) {
            return Optional.of(new GeocodeResult(latitude, longitude,
                    ((Number) bbox.get(1)).doubleValue(), ((Number) bbox.get(0)).doubleValue(),
                    ((Number) bbox.get(3)).doubleValue(), ((Number) bbox.get(2)).doubleValue()));
        }
        return Optional.of(new GeocodeResult(latitude, longitude, null, null, null, null));
    }

    private static String toAlpha3(String isoCode) {
        try {
            return Locale.of("", isoCode).getISO3Country();
        } catch (java.util.MissingResourceException e) {
            return isoCode;
        }
    }
}
