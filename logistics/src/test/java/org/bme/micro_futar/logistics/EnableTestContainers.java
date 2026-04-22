package org.bme.micro_futar.logistics;

import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(initializers = TestcontainersConfiguration.class)
public @interface EnableTestContainers {
}
