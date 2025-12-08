package tech.erben.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/os")
public class OperatingSystemController {

    @Autowired
    private OperatingSystem operatingSystem;

    @GetMapping
    public String getOs() throws IOException {
        return this.operatingSystem.writeOsInfo();
    }
}
