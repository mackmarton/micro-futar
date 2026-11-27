package org.bme.micro_futar.logistics.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;

/**
 * Sends to and reads from the application's topics. Topics are named by their key under {@code kafka.topics},
 * for example {@code "shipment-topic"}.
 */
public class KafkaTestClient {

    private final String bootstrapServers;
    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final KafkaListenerEndpointRegistry listeners;
    private final KafkaTemplate<String, String> applicationTemplate;
    private final Map<TopicPartition, Long> testStartOffsets = new HashMap<>();

    KafkaTestClient(String bootstrapServers, Environment environment, ObjectMapper objectMapper,
                    KafkaListenerEndpointRegistry listeners, KafkaTemplate<String, String> applicationTemplate) {
        this.bootstrapServers = bootstrapServers;
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.listeners = listeners;
        this.applicationTemplate = applicationTemplate;
    }

    /** Sends the payload as JSON (a String is sent as it is) and waits for the broker to acknowledge it. */
    public void send(String topicKey, String key, Object payload) {
        sendRecord(topicKey, key, payload);
    }

    /**
     * Sends the payload and returns once the application's listeners of the topic have committed it, and whatever
     * they published while handling it is on the broker. The state can then be asserted without waiting, also when
     * the message is expected to change nothing. A listener that keeps failing commits too, after its retries.
     */
    public void sendAndAwaitConsumption(String topicKey, String key, Object payload) {
        List<String> groups = listenerGroups(topic(topicKey));
        RecordMetadata sent = sendRecord(topicKey, key, payload);
        TopicPartition partition = new TopicPartition(sent.topic(), sent.partition());

        try (var admin = Admin.create(Map.<String, Object>of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers))) {
            await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(50)).until(() ->
                    allCommittedPast(admin, groups, partition, sent.offset()));
        }
        applicationTemplate.flush();
    }

    private RecordMetadata sendRecord(String topicKey, String key, Object payload) {
        Map<String, Object> config = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (var producer = new KafkaProducer<>(config, new StringSerializer(), new StringSerializer())) {
            return producer.send(new ProducerRecord<>(topic(topicKey), key, json(payload))).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Could not send to " + topicKey, e);
        }
    }

    /**
     * Everything that has arrived on the topic since the current test started. It does not wait for more, so wrap
     * assertions on asynchronous publishing in Awaitility.
     */
    public List<ConsumerRecord<String, String>> received(String topicKey) {
        String topic = topic(topicKey);
        try (var consumer = consumer()) {
            List<TopicPartition> partitions = partitions(consumer, topic);
            consumer.assign(partitions);
            partitions.forEach(p -> consumer.seek(p, testStartOffsets.getOrDefault(p, 0L)));
            Map<TopicPartition, Long> end = consumer.endOffsets(partitions);

            List<ConsumerRecord<String, String>> records = new ArrayList<>();
            while (partitions.stream().anyMatch(p -> consumer.position(p) < end.get(p))) {
                consumer.poll(Duration.ofMillis(100)).forEach(records::add);
            }
            return records;
        }
    }

    /** {@link #received(String)}, with the values read from JSON. */
    public <T> List<T> received(String topicKey, Class<T> type) {
        return received(topicKey).stream()
                .map(record -> read(record.value(), type))
                .toList();
    }

    void markTestStart() {
        testStartOffsets.clear();
        Map<String, String> topics = Binder.get(environment)
                .bind("kafka.topics", Bindable.mapOf(String.class, String.class))
                .get();
        try (var consumer = consumer()) {
            topics.values().forEach(topic ->
                    testStartOffsets.putAll(consumer.endOffsets(partitions(consumer, topic))));
        }
    }

    private String topic(String topicKey) {
        return environment.getRequiredProperty("kafka.topics." + topicKey);
    }

    private String json(Object payload) {
        if (payload instanceof String text) {
            return text;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> listenerGroups(String topic) {
        List<String> groups = listeners.getListenerContainers().stream()
                .filter(container -> listensTo(container, topic))
                .map(MessageListenerContainer::getGroupId)
                .toList();
        if (groups.isEmpty()) {
            throw new IllegalStateException("The application has no listener on " + topic);
        }
        return groups;
    }

    private static boolean listensTo(MessageListenerContainer container, String topic) {
        String[] topics = container.getContainerProperties().getTopics();
        return topics != null && Arrays.asList(topics).contains(topic);
    }

    private static boolean allCommittedPast(Admin admin, List<String> groups, TopicPartition partition, long offset)
            throws ExecutionException, InterruptedException {
        for (String group : groups) {
            OffsetAndMetadata committed = admin.listConsumerGroupOffsets(group)
                    .partitionsToOffsetAndMetadata().get()
                    .get(partition);
            // the committed offset is the next one to read; there is none before the group's first commit
            if (committed == null || committed.offset() <= offset) {
                return false;
            }
        }
        return true;
    }

    private KafkaConsumer<String, String> consumer() {
        Map<String, Object> config = Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaConsumer<>(config, new StringDeserializer(), new StringDeserializer());
    }

    private static List<TopicPartition> partitions(KafkaConsumer<String, String> consumer, String topic) {
        return consumer.partitionsFor(topic).stream()
                .map(info -> new TopicPartition(info.topic(), info.partition()))
                .toList();
    }
}
