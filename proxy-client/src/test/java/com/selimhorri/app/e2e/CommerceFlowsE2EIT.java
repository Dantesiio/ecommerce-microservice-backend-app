package com.selimhorri.app.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.github.tomakehurst.wiremock.client.WireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.discovery.client.simple.instances.USER-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.discovery.client.simple.instances.PRODUCT-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.discovery.client.simple.instances.ORDER-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.discovery.client.simple.instances.PAYMENT-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.discovery.client.simple.instances.SHIPPING-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.discovery.client.simple.instances.FAVOURITE-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
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
class CommerceFlowsE2EIT {

    private static final String CONTEXT_PATH = "/app";
    private static final String TIMESTAMP = "01-01-2024__00:00:00:000000";
    private static final String ENCODED_TIMESTAMP = URLEncoder.encode(TIMESTAMP, StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetWireMocks() {
        reset();
    }

    @Test
    @DisplayName("Flujo E2E: registro de usuario y lectura posterior")
    void registrationAndLookupFlow() throws Exception {
    stubFor(WireMock.post(urlEqualTo("/user-service/api/users"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"userId\":101," +
                    "\"firstName\":\"John\"," +
                    "\"lastName\":\"Doe\"," +
                    "\"email\":\"john.doe@example.com\"}")));

    stubFor(WireMock.post(urlEqualTo("/user-service/api/credentials"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"credentialId\":201," +
                    "\"username\":\"john.doe\"}")));

    stubFor(WireMock.get(urlEqualTo("/user-service/api/users/username/john.doe"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"userId\":101," +
                    "\"firstName\":\"John\"," +
                    "\"email\":\"john.doe@example.com\"}")));

        mockMvc.perform(post("/app/api/users").contextPath(CONTEXT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"firstName\":\"John\"," +
                    "\"lastName\":\"Doe\"," +
                    "\"email\":\"john.doe@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(101));

        mockMvc.perform(post("/app/api/credentials").contextPath(CONTEXT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"username\":\"john.doe\"," +
                    "\"password\":\"s3cr3t\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.credentialId").value(201));

        mockMvc.perform(get("/app/api/users/username/{username}", "john.doe").contextPath(CONTEXT_PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(101))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Flujo E2E: compra completa con pago y envío")
    void completePurchaseFlow() throws Exception {
    stubFor(WireMock.get(urlEqualTo("/product-service/api/products/501"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"productId\":501," +
                    "\"productTitle\":\"Gaming Keyboard\"," +
                    "\"priceUnit\":89.99}")));

    stubFor(WireMock.post(urlEqualTo("/order-service/api/orders"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"orderId\":7001," +
                    "\"orderDesc\":\"New order\"," +
                    "\"orderFee\":109.99}")));

    stubFor(WireMock.post(urlEqualTo("/payment-service/api/payments"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"paymentId\":301," +
                    "\"isPayed\":true," +
                    "\"paymentStatus\":\"COMPLETED\"}")));

    stubFor(WireMock.post(urlEqualTo("/shipping-service/api/shippings"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"orderId\":7001," +
                    "\"productId\":501," +
                    "\"orderedQuantity\":1}")));

        mockMvc.perform(get("/app/api/products/{id}", "501").contextPath(CONTEXT_PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(501))
            .andExpect(jsonPath("$.productTitle").value("Gaming Keyboard"));

        mockMvc.perform(post("/app/api/orders").contextPath(CONTEXT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"orderDesc\":\"New order\"," +
                    "\"orderFee\":109.99," +
                    "\"cart\":{\"cartId\":77}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(7001));

        mockMvc.perform(post("/app/api/payments").contextPath(CONTEXT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"order\":{\"orderId\":7001}," +
                    "\"paymentStatus\":\"COMPLETED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(301))
            .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));

        mockMvc.perform(post("/app/api/shippings").contextPath(CONTEXT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"order\":{\"orderId\":7001}," +
                    "\"product\":{\"productId\":501}," +
                    "\"orderedQuantity\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(7001))
            .andExpect(jsonPath("$.orderedQuantity").value(1));
    }

    @Test
    @DisplayName("Flujo E2E: consulta de histórico de órdenes")
    void orderHistoryFlow() throws Exception {
    stubFor(WireMock.get(urlEqualTo("/order-service/api/orders"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"collection\":[{" +
                    "\"orderId\":8001," +
                    "\"orderDesc\":\"Historic order\"}]}")));

        mockMvc.perform(get("/app/api/orders").contextPath(CONTEXT_PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[0].orderId").value(8001))
            .andExpect(jsonPath("$.collection[0].orderDesc").value("Historic order"));
    }

    @Test
    @DisplayName("Flujo E2E: gestión de favoritos")
    void favouriteManagementFlow() throws Exception {
    stubFor(WireMock.post(urlEqualTo("/favourite-service/api/favourites"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"user\":{\"userId\":101}," +
                    "\"product\":{\"productId\":501}," +
                    "\"likeDate\":\"" + TIMESTAMP + "\"}")));

    stubFor(WireMock.get(urlEqualTo("/favourite-service/api/favourites"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"collection\":[{" +
                    "\"user\":{\"userId\":101}," +
                    "\"product\":{\"productId\":501}," +
                    "\"likeDate\":\"" + TIMESTAMP + "\"}]}")));

    stubFor(WireMock.delete(urlEqualTo("/favourite-service/api/favourites/101/501/" + ENCODED_TIMESTAMP))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("true")));

        mockMvc.perform(post("/app/api/favourites").contextPath(CONTEXT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"userId\":101," +
                    "\"productId\":501," +
                    "\"likeDate\":\"" + TIMESTAMP + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.userId").value(101))
            .andExpect(jsonPath("$.product.productId").value(501));

        mockMvc.perform(get("/app/api/favourites").contextPath(CONTEXT_PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[0].user.userId").value(101))
            .andExpect(jsonPath("$.collection[0].likeDate").value(TIMESTAMP));

        mockMvc.perform(delete("/app/api/favourites/{userId}/{productId}/{likeDate}", "101", "501", TIMESTAMP).contextPath(CONTEXT_PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Flujo E2E: actualización de una orden existente")
    void orderUpdateFlow() throws Exception {
    stubFor(WireMock.put(urlEqualTo("/order-service/api/orders/9001"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"orderId\":9001," +
                    "\"orderDesc\":\"Updated order\"," +
                    "\"orderFee\":150.0}")));

    stubFor(WireMock.put(urlEqualTo("/shipping-service/api/shippings"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"orderId\":9001," +
                    "\"productId\":501," +
                    "\"orderedQuantity\":2}")));

        mockMvc.perform(put("/app/api/orders/{orderId}", "9001").contextPath(CONTEXT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"orderDesc\":\"Updated order\"," +
                    "\"orderFee\":150.0," +
                    "\"cart\":{\"cartId\":99}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(9001))
            .andExpect(jsonPath("$.orderDesc").value("Updated order"));

        mockMvc.perform(put("/app/api/shippings").contextPath(CONTEXT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"order\":{\"orderId\":9001}," +
                    "\"product\":{\"productId\":501}," +
                    "\"orderedQuantity\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderedQuantity").value(2));
    }
}
