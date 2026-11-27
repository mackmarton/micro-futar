package org.bme.micro_futar.courier;

import org.bme.micro_futar.courier.support.IntegrationTestExtension;
import org.bme.micro_futar.courier.support.TestSupportConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The one shared context of the integration tests. The database is emptied before every test.
 * Do not add {@code @MockitoBean} to a test class, it would start a second context.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, TestSupportConfiguration.class})
@ExtendWith(IntegrationTestExtension.class)
public @interface IntegrationTest {
}
