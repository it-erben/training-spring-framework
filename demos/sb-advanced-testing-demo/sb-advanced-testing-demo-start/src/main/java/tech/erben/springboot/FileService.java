package tech.erben.springboot;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FileService {
    public Map<String, String> getFileInfo(String id) {
        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("id", id);
        fileInfo.put("name", "example.txt");
        return fileInfo;
    }
}
