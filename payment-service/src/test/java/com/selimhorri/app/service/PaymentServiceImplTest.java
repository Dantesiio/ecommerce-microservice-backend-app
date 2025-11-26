package com.selimhorri.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.exception.wrapper.PaymentNotFoundException;
import com.selimhorri.app.repository.PaymentRepository;
import com.selimhorri.app.service.impl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("findById enriches payment with order details")
    void findByIdEnrichesOrder() {
        Payment payment = samplePayment();
        when(paymentRepository.findById(12)).thenReturn(Optional.of(payment));
        OrderDto orderDto = OrderDto.builder().orderId(7).orderDesc("order").build();
        when(restTemplate.getForObject(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + 7, OrderDto.class))
            .thenReturn(orderDto);

        PaymentDto result = paymentService.findById(12);

        assertThat(result.getOrderDto()).isNotNull();
        assertThat(result.getOrderDto().getOrderDesc()).isEqualTo("order");
    }

    @Test
    @DisplayName("findAll fetches orders for every payment")
    void findAllFetchesOrders() {
        Payment payment = samplePayment();
        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        when(restTemplate.getForObject(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + 7, OrderDto.class))
            .thenReturn(OrderDto.builder().orderId(7).orderDesc("bulk").build());

        List<PaymentDto> payments = paymentService.findAll();

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getOrderDto().getOrderDesc()).isEqualTo("bulk");
    }

    @Test
    @DisplayName("findById throws when payment missing")
    void findByIdMissingThrows() {
        when(paymentRepository.findById(77)).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> paymentService.findById(77))
            .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("save creates new payment")
    void saveCreatesPayment() {
        Payment payment = samplePayment();
        PaymentDto dto = PaymentDto.builder()
            .orderDto(OrderDto.builder().orderId(7).build())
            .isPayed(true)
            .paymentStatus(PaymentStatus.COMPLETED)
            .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto result = paymentService.save(dto);

        assertThat(result).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("update updates existing payment")
    void updateUpdatesPayment() {
        Payment payment = samplePayment();
        PaymentDto dto = PaymentDto.builder()
            .paymentId(12)
            .orderDto(OrderDto.builder().orderId(7).build())
            .isPayed(false)
            .paymentStatus(PaymentStatus.IN_PROGRESS)
            .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto result = paymentService.update(dto);

        assertThat(result).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("deleteById delegates to repository")
    void deleteByIdDelegates() {
        paymentService.deleteById(5);

        verify(paymentRepository).deleteById(5);
    }

    private Payment samplePayment() {
        return Payment.builder()
            .paymentId(12)
            .orderId(7)
            .isPayed(true)
            .paymentStatus(PaymentStatus.COMPLETED)
            .build();
    }
}
