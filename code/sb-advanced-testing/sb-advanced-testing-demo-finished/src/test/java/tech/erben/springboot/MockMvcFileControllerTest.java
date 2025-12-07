package tech.erben.springboot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FileController.class)
public class MockMvcFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testHttpGetWithJsonExpectations() throws Exception {
        String fileId = "1";
        mockMvc
            .perform(get("/files?id=" + fileId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(fileId))
            .andExpect(jsonPath("$.name").value("example.txt"));
    }

    @Test
    public void testHttpFileUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test_file_upload".getBytes(StandardCharsets.UTF_8)
        );
        mockMvc
            .perform(multipart("/files/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(content().string("File uploaded successfully."));
    }

    @Test
    public void testHttpPostWithJson() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", "value");

        mockMvc
            .perform(
                post("/files/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.key").value("value"));
    }
}
