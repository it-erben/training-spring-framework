package tech.erben.springboot.webdemo.web;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Duration.ofSeconds(10).toMillis());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    emitter.send(
                        SseEmitter
                            .event()
                            .name("update")
                            .data("Book catalog heartbeat " + i)
                    );
                    Thread.sleep(500);
                }
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            } finally {
                executor.shutdown();
            }
        });
        return emitter;
    }
}
