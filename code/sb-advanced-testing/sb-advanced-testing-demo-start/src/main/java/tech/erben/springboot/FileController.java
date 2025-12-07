package tech.erben.springboot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController {

    @GetMapping
    public Map<String, String> getFileInfo(@RequestParam("id") String id) {
        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("id", id);
        fileInfo.put("name", "example.txt");

        return fileInfo;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        System.out.println(new String(file.getBytes()));
        return ResponseEntity.ok("File uploaded successfully.");
    }

    @PostMapping("/create")
    public Map<String, String> createFile(@RequestBody Map<String, String> input) {
        return input;
    }
}
