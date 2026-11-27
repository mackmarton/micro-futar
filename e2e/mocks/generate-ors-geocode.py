#!/usr/bin/env python3
"""Writes the WireMock mappings of the ORS geocoding stubs. Run after editing CITIES."""
import json
import pathlib

# file name: (name regex, ISO-2, ISO-3, [lon, lat], [minLon, minLat, maxLon, maxLat])
CITIES = {
    "budapest": ("budapest", "HU", "HUN", [19.0402, 47.4979], [18.925, 47.349, 19.335, 47.613]),
    "gyor": ("gy[őo]r", "HU", "HUN", [17.6504, 47.6875], [17.55, 47.62, 17.75, 47.75]),
    "debrecen": ("debrecen", "HU", "HUN", [21.6273, 47.5316], [21.45, 47.42, 21.78, 47.65]),
    "wien": ("b[ée]cs|wien|vienna", "AT", "AUT", [16.3738, 48.2082], [16.182, 48.118, 16.578, 48.323]),
    "graz": ("graz", "AT", "AUT", [15.4395, 47.0707], [15.35, 47.01, 15.53, 47.13]),
    "linz": ("linz", "AT", "AUT", [14.2858, 48.3069], [14.24, 48.24, 14.41, 48.38]),
}


def mapping(request, features, priority):
    return {
        "priority": priority,
        "request": {"method": "GET", "urlPath": "/geocode/search", **request},
        "response": {
            "status": 200,
            "headers": {"Content-Type": "application/json"},
            "jsonBody": {"type": "FeatureCollection", "features": features},
        },
    }


out = pathlib.Path(__file__).parent / "ors" / "mappings"
out.mkdir(parents=True, exist_ok=True)
for key, (pattern, iso2, iso3, center, bbox) in CITIES.items():
    request = {"queryParameters": {"text": {"matches": f"(?iu)({pattern})"},
                                   "boundary.country": {"equalTo": iso2}}}
    feature = {"type": "Feature", "properties": {"country_a": iso3},
               "geometry": {"type": "Point", "coordinates": center}, "bbox": bbox}
    (out / f"geocode-{key}.json").write_text(json.dumps(mapping(request, [feature], 1), indent=2) + "\n")
# any other city: no match, as ORS answers for an unknown name
(out / "geocode-unknown.json").write_text(json.dumps(mapping({}, [], 10), indent=2) + "\n")
