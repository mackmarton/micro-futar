package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DepoRouteFinderService {

    private final DepoTransitService depoTransitService;

    public List<Long> findCheapestRoute(Long originDepoId, Long destinationDepoId, Long packageSizeId) {
        return findCheapestTransits(originDepoId, destinationDepoId, packageSizeId)
                .map(transits -> {
                    List<Long> depoIds = new ArrayList<>();
                    depoIds.add(originDepoId);
                    transits.forEach(t -> depoIds.add(t.getDestinationDepoId()));
                    return depoIds;
                })
                .orElseGet(List::of);
    }

    /**
     * The transits (edges) of the cheapest path, in order; empty list when origin equals destination,
     * Optional.empty() when no path exists.
     */
    public Optional<List<DepoTransitDTO>> findCheapestTransits(Long originDepoId, Long destinationDepoId, Long packageSizeId) {
        List<DepoTransitDTO> allTransits = depoTransitService.getAllDepoTransits();

        List<DepoTransitDTO> relevantTransits = allTransits.stream()
                .filter(transit -> transit.getPackageSizeId().equals(packageSizeId))
                .toList();

        Map<Long, List<Edge>> graph = buildGraph(relevantTransits);
        return dijkstra(graph, originDepoId, destinationDepoId);
    }

    public Double findCheapestRoutePrice(Long originDepoId, Long destinationDepoId, Long packageSizeId) {
        List<DepoTransitDTO> allTransits = depoTransitService.getAllDepoTransits();

        List<DepoTransitDTO> relevantTransits = allTransits.stream()
                .filter(transit -> transit.getPackageSizeId().equals(packageSizeId))
                .toList();

        Map<Long, List<Edge>> graph = buildGraph(relevantTransits);
        return dijkstraPrice(graph, originDepoId, destinationDepoId);
    }

    private Map<Long, List<Edge>> buildGraph(List<DepoTransitDTO> transits) {
        Map<Long, List<Edge>> graph = new HashMap<>();

        for (DepoTransitDTO transit : transits) {
            graph.computeIfAbsent(transit.getOriginDepoId(), k -> new ArrayList<>())
                .add(new Edge(transit.getDestinationDepoId(), transit.getPrice(), transit));
        }

        return graph;
    }

    private Optional<List<DepoTransitDTO>> dijkstra(Map<Long, List<Edge>> graph, Long start, Long end) {
        // Distance map: depoId -> minimum cost to reach it
        Map<Long, Double> distances = new HashMap<>();
        // Parent map: depoId -> previous depoId in shortest path
        Map<Long, Edge> parents = new HashMap<>();
        Map<Long, Long> parentDepos = new HashMap<>();
        // Priority queue: sorts by distance
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        // Visited set
        Set<Long> visited = new HashSet<>();

        // Initialize
        distances.put(start, 0.0);
        pq.offer(new Node(start, 0.0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            Long currentDepoId = current.depoId;

            // Skip if already visited
            if (visited.contains(currentDepoId)) {
                continue;
            }

            visited.add(currentDepoId);

            // If we reached the destination, reconstruct path
            if (currentDepoId.equals(end)) {
                return Optional.of(reconstructPath(parents, parentDepos, start, end));
            }

            // Get neighbors
            List<Edge> neighbors = graph.getOrDefault(currentDepoId, Collections.emptyList());

            for (Edge edge : neighbors) {
                if (visited.contains(edge.destinationDepoId)) {
                    continue;
                }

                double newDistance = distances.get(currentDepoId) + edge.cost;
                double currentDistance = distances.getOrDefault(edge.destinationDepoId, Double.MAX_VALUE);

                if (newDistance < currentDistance) {
                    distances.put(edge.destinationDepoId, newDistance);
                    parents.put(edge.destinationDepoId, edge);
                    parentDepos.put(edge.destinationDepoId, currentDepoId);
                    pq.offer(new Node(edge.destinationDepoId, newDistance));
                }
            }
        }

        // No path found
        return Optional.empty();
    }

    private Double dijkstraPrice(Map<Long, List<Edge>> graph, Long start, Long end) {
        // Distance map: depoId -> minimum cost to reach it
        Map<Long, Double> distances = new HashMap<>();
        // Priority queue: sorts by distance
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        // Visited set
        Set<Long> visited = new HashSet<>();

        // Initialize
        distances.put(start, 0.0);
        pq.offer(new Node(start, 0.0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            Long currentDepoId = current.depoId;

            // Skip if already visited
            if (visited.contains(currentDepoId)) {
                continue;
            }

            visited.add(currentDepoId);

            // If we reached the destination, return the cost
            if (currentDepoId.equals(end)) {
                return distances.get(end);
            }

            // Get neighbors
            List<Edge> neighbors = graph.getOrDefault(currentDepoId, Collections.emptyList());

            for (Edge edge : neighbors) {
                if (visited.contains(edge.destinationDepoId)) {
                    continue;
                }

                double newDistance = distances.get(currentDepoId) + edge.cost;
                double currentDistance = distances.getOrDefault(edge.destinationDepoId, Double.MAX_VALUE);

                if (newDistance < currentDistance) {
                    distances.put(edge.destinationDepoId, newDistance);
                    pq.offer(new Node(edge.destinationDepoId, newDistance));
                }
            }
        }

        // No path found
        return null;
    }

    private List<DepoTransitDTO> reconstructPath(Map<Long, Edge> parents, Map<Long, Long> parentDepos, Long start, Long end) {
        List<DepoTransitDTO> path = new ArrayList<>();
        Long current = end;

        while (!current.equals(start)) {
            path.add(parents.get(current).transit);
            current = parentDepos.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    private static class Edge {
        Long destinationDepoId;
        Double cost;
        DepoTransitDTO transit;

        Edge(Long destinationDepoId, Double cost, DepoTransitDTO transit) {
            this.destinationDepoId = destinationDepoId;
            this.cost = cost;
            this.transit = transit;
        }
    }

    private static class Node {
        Long depoId;
        Double distance;

        Node(Long depoId, Double distance) {
            this.depoId = depoId;
            this.distance = distance;
        }
    }
}

