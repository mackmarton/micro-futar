package org.bme.micro_futar.tracking;

import org.bme.micro_futar.tracking.support.IntegrationTestExtension;
import org.bme.micro_futar.tracking.support.TestSupportConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The one shared context of the integration tests. The database is emptied before every test.
 * External systems are mocked here, once for all tests; do not add {@code @MockitoBean} to a test class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, TestSupportConfiguration.class})
@ExtendWith(IntegrationTestExtension.class)
@MockitoBean(types = JavaMailSender.class)
public @interface IntegrationTest {
}
