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
        List<DepoTransitDTO> allTransits = depoTransitService.getAllDepoTransits();

        List<DepoTransitDTO> relevantTransits = allTransits.stream()
                .filter(transit -> transit.getPackageSizeId().equals(packageSizeId))
                .toList();

        Map<Long, List<Edge>> graph = buildGraph(relevantTransits);
        return dijkstra(graph, originDepoId, destinationDepoId);
    }

    private Map<Long, List<Edge>> buildGraph(List<DepoTransitDTO> transits) {
        Map<Long, List<Edge>> graph = new HashMap<>();

        for (DepoTransitDTO transit : transits) {
            graph.computeIfAbsent(transit.getOriginDepoId(), k -> new ArrayList<>())
                .add(new Edge(transit.getDestinationDepoId(), transit.getPrice()));
        }

        return graph;
    }

    private List<Long> dijkstra(Map<Long, List<Edge>> graph, Long start, Long end) {
        // Distance map: depoId -> minimum cost to reach it
        Map<Long, Double> distances = new HashMap<>();
        // Parent map: depoId -> previous depoId in shortest path
        Map<Long, Long> parents = new HashMap<>();
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
                return reconstructPath(parents, start, end);
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
                    parents.put(edge.destinationDepoId, currentDepoId);
                    pq.offer(new Node(edge.destinationDepoId, newDistance));
                }
            }
        }

        // No path found
        return Collections.emptyList();
    }

    private List<Long> reconstructPath(Map<Long, Long> parents, Long start, Long end) {
        List<Long> path = new ArrayList<>();
        Long current = end;

        while (current != null) {
            path.add(current);
            if (current.equals(start)) {
                break;
            }
            current = parents.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    private static class Edge {
        Long destinationDepoId;
        Double cost;

        Edge(Long destinationDepoId, Double cost) {
            this.destinationDepoId = destinationDepoId;
            this.cost = cost;
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

