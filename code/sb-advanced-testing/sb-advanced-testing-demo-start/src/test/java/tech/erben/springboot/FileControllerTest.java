package tech.erben.springboot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @BeforeEach
    void setUp() {}

    @Test
    public void test() throws Exception {

        String returnedFiled = UUID.randomUUID().toString();
        Mockito.when(fileService.getFileInfo(Mockito.any())).thenReturn(Map.of("name", returnedFiled));

        mockMvc.perform(get("/files?id=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpload() throws Exception {
        byte[] content = "test_file_upload".getBytes();
        String filename = "test.txt";
        MockMultipartFile file = new MockMultipartFile("file", filename, MediaType.TEXT_PLAIN_VALUE, content);

        mockMvc.perform(multipart("/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully."));
    }
}
