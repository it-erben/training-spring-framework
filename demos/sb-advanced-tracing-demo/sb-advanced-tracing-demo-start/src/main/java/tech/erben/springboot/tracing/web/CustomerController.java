package tech.erben.springboot.tracing.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.erben.springboot.tracing.api.CreateOrderRequest;
import tech.erben.springboot.tracing.api.CustomerResponse;
import tech.erben.springboot.tracing.api.OrderResponse;
import tech.erben.springboot.tracing.service.TracingService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final TracingService tracingService;

    public CustomerController(TracingService tracingService) {
        this.tracingService = tracingService;
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(
            @PathVariable long id,
            @RequestParam(defaultValue = "true") boolean recommendations
    ) {
        return tracingService.loadCustomer(id, recommendations);
    }

    @PostMapping("/{id}/orders")
    public OrderResponse createOrder(
            @PathVariable long id,
            @RequestBody CreateOrderRequest request
    ) {
        return tracingService.createOrder(id, request);
    }
}
