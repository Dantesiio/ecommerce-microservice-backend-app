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

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.repository.PaymentRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.discovery.client.simple.instances.ORDER-SERVICE[0].uri=http://localhost:${wiremock.server.port}"
    })
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("dev")
class PaymentResourceIT {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        reset();
    }

    @Test
    @DisplayName("GET /api/payments/{id} consulta ORDER-SERVICE para detalles de la orden")
    void findByIdFetchesOrderDetails() throws Exception {
        Payment payment = paymentRepository.save(Payment.builder()
            .orderId(123)
            .isPayed(true)
            .paymentStatus(PaymentStatus.COMPLETED)
            .build());

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/order-service/api/orders/123"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{" +
                    "\"orderId\":123," +
                    "\"orderDesc\":\"Mocked order\"}")));

        mockMvc.perform(get("/payment-service/api/payments/{id}", payment.getPaymentId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(payment.getPaymentId()))
            .andExpect(jsonPath("$.order.orderId").value(123))
            .andExpect(jsonPath("$.order.orderDesc").value("Mocked order"));
    }
}
