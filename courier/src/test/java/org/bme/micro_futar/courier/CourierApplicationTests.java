package org.bme.micro_futar.courier;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CourierApplicationTests {

	@Test
	void contextLoads() {
	}

}
