package tech.erben.springboot.kafkademo.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tech.erben.springboot.kafkademo.model.BroadcastMessage;
import tech.erben.springboot.kafkademo.model.OrderMessage;
import tech.erben.springboot.kafkademo.model.ProcessedMessage;
import tech.erben.springboot.kafkademo.service.InMemoryDeliveryLog;
import tech.erben.springboot.kafkademo.service.OrderMessagingService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessagingDemoController {

    private final OrderMessagingService messagingService;
    private final InMemoryDeliveryLog deliveryLog;

    public MessagingDemoController(
        OrderMessagingService messagingService,
        InMemoryDeliveryLog deliveryLog
    ) {
        this.messagingService = messagingService;
        this.deliveryLog = deliveryLog;
    }

    @PostMapping("/orders")
    public OrderMessage publishOrder(@Valid @RequestBody OrderRequest request) {
        return messagingService.sendOrder(request);
    }

    @PostMapping("/announcements")
    public BroadcastMessage publishAnnouncement(
        @Valid @RequestBody AnnouncementRequest request
    ) {
        return messagingService.broadcast(request);
    }

    @GetMapping("/logs")
    public List<ProcessedMessage> logs(
        @RequestParam(name = "topic", required = false) String topic
    ) {
        if (topic == null || topic.isBlank()) {
            return deliveryLog.all();
        }
        return deliveryLog.forTopic(topic);
    }

    @DeleteMapping("/logs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearLogs() {
        deliveryLog.clear();
    }
}
