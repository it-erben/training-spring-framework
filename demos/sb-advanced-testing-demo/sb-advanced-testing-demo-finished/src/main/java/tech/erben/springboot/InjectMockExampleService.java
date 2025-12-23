package tech.erben.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InjectMockExampleService {

    @Autowired
    MockMeBean mockMeBean;

    public String callMockMeBean() {
        return mockMeBean.mockMe();
    }
}
