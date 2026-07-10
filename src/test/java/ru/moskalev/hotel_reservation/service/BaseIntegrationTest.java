package ru.moskalev.hotel_reservation.service;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

@SpringBootTest
public abstract class BaseIntegrationTest {

    private static final String TEST_KAFKA_GROUP_ID = "test-group-" + UUID.randomUUID();

    protected static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withReuse(true)
                    .withCommand(
                            "-c", "max_connections=300"
                    );

    protected static final MongoDBContainer mongo =
            new MongoDBContainer("mongo:7.0")
                    .withReuse(true);

    protected static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse(   "apache/kafka:3.8.0"))
                    .withReuse(true);


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);


        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> TEST_KAFKA_GROUP_ID);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        mongo.start();
        kafka.start();
    }
}