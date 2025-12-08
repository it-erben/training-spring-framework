package tech.erben.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/os")
public class OsController {

    @Autowired
    private OperatingSystem os;

    @GetMapping
    private String os() throws IOException {
        return os.writeOsInfo();
    }

}
