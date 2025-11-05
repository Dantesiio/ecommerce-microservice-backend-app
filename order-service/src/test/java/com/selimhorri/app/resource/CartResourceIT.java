package com.selimhorri.app.resource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.repository.CartRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.discovery.client.simple.instances.USER-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
        "wiremock.server.https-port=-1",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.zipkin.enabled=false",
        "spring.cloud.config.enabled=false",
        "SPRING_CONFIG_IMPORT=optional:file:./"
    })
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("dev")
class CartResourceIT {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        cartRepository.deleteAll();
        reset();
    }

    @Test
    @DisplayName("GET /api/carts/{id} enriches user information via USER-SERVICE")
    void findByIdIncludesUserFromUserService() throws Exception {
        Cart cart = cartRepository.save(Cart.builder().userId(55).build());

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/user-service/api/users/55"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"userId\":55," +
                    "\"firstName\":\"Alice\"," +
                    "\"lastName\":\"Smith\"," +
                    "\"email\":\"alice@example.com\"}")));

    mockMvc.perform(get("/order-service/api/carts/{id}", cart.getCartId())
        .contextPath("/order-service"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cartId").value(cart.getCartId()))
            .andExpect(jsonPath("$.user.userId").value(55))
            .andExpect(jsonPath("$.user.firstName").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/carts devuelve colección con usuarios enriquecidos")
    void findAllEnrichesUserList() throws Exception {
        cartRepository.save(Cart.builder().userId(77).build());

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/user-service/api/users/77"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"userId\":77," +
                    "\"firstName\":\"Bob\"}")));

    mockMvc.perform(get("/order-service/api/carts")
        .contextPath("/order-service"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[0].user.userId").value(77))
            .andExpect(jsonPath("$.collection[0].user.firstName").value("Bob"));
    }
}
