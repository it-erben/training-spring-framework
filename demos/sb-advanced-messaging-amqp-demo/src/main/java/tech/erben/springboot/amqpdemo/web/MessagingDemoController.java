package tech.erben.springboot.amqpdemo.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tech.erben.springboot.amqpdemo.model.BroadcastMessage;
import tech.erben.springboot.amqpdemo.model.OrderMessage;
import tech.erben.springboot.amqpdemo.model.ProcessedMessage;
import tech.erben.springboot.amqpdemo.service.InMemoryDeliveryLog;
import tech.erben.springboot.amqpdemo.service.OrderMessagingService;

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
        @RequestParam(name = "queue", required = false) String queue
    ) {
        if (queue == null || queue.isBlank()) {
            return deliveryLog.all();
        }
        return deliveryLog.forQueue(queue);
    }

    @DeleteMapping("/logs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearLogs() {
        deliveryLog.clear();
    }
}
