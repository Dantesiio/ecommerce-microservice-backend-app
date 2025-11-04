package com.selimhorri.app.business.order.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.discovery.client.simple.instances.ORDER-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
        "wiremock.server.https-port=-1",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.config.enabled=false",
        "SPRING_CONFIG_IMPORT=optional:file:./"
    })
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("dev")
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetWireMock() {
        reset();
    }

    @Test
    @DisplayName("GET /api/orders delega en ORDER-SERVICE")
    void getOrdersDelegatesToOrderService() throws Exception {
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/order-service/api/orders"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"collection\":[{" +
                    "\"orderId\":41," +
                    "\"orderDesc\":\"Gateway order\"}" +
                    "]}")));

    mockMvc.perform(get("/app/api/orders").contextPath("/app"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[0].orderId").value(41))
            .andExpect(jsonPath("$.collection[0].orderDesc").value("Gateway order"));
    }

    @Test
    @DisplayName("POST /api/orders reenvía la creación al ORDER-SERVICE")
    void postOrderDelegatesToOrderService() throws Exception {
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/order-service/api/orders"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"orderId\":42," +
                    "\"orderDesc\":\"Created via proxy\"}")));

        mockMvc.perform(post("/app/api/orders").contextPath("/app")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"orderDesc\":\"Created via proxy\"," +
                    "\"orderFee\":120.0," +
                    "\"cart\":{\"cartId\":5}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(42))
            .andExpect(jsonPath("$.orderDesc").value("Created via proxy"));
    }
}
