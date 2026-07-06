package codehealthy.payflux.gatewayservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class GatewaySecurityConfigTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.webAppContextSetup(webApplicationContext)
				.apply(springSecurity())
				.build();
	}

	@Test
	void gatewayHealthIsPublic() throws Exception {
		mockMvc.perform(get("/gateway/health"))
				.andExpect(status().isOk());
	}

	@Test
	void protectedRoutesRequireAuthentication() throws Exception {
		mockMvc.perform(get("/accounts"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void auditRouteRequiresAuthenticationBeforeRouting() throws Exception {
		mockMvc.perform(get("/audit-records"))
				.andExpect(status().isUnauthorized());
	}
}
