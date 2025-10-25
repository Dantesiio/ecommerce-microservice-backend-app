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

import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.repository.OrderItemRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.discovery.client.simple.instances.ORDER-SERVICE[0].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.discovery.client.simple.instances.PRODUCT-SERVICE[0].uri=http://localhost:${wiremock.server.port}"
    })
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("dev")
class OrderItemResourceIT {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        reset();
    }

    @Test
    @DisplayName("GET /api/shippings enriquece cada item con producto y orden")
    void findAllFetchesProductAndOrder() throws Exception {
        orderItemRepository.save(OrderItem.builder()
            .productId(501)
            .orderId(900)
            .orderedQuantity(2)
            .build());

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/product-service/api/products/501"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"productId\":501," +
                    "\"productTitle\":\"Headphones\"}")));

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/order-service/api/orders/900"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"orderId\":900," +
                    "\"orderDesc\":\"Shipping order\"}")));

        mockMvc.perform(get("/shipping-service/api/shippings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection[0].product.productId").value(501))
            .andExpect(jsonPath("$.collection[0].product.productTitle").value("Headphones"))
            .andExpect(jsonPath("$.collection[0].order.orderId").value(900))
            .andExpect(jsonPath("$.collection[0].order.orderDesc").value("Shipping order"));
    }
}
