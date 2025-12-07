package tech.erben.springboot;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = BasicSpringApplication.class)
public class ApplicationContextTest {

    @Autowired
    HelloService helloService;

    @Value("${tech.erben.springboot.testing.helloService.message}")
    String message;

    @Autowired
    ApplicationContext context;

    @MockBean
    MockMeBean mockMeBean;

    @Autowired
    InjectMockExampleService injectMockExampleService;

    @Test
    public void basicTest() {
        System.out.println(helloService.sayHello());
        System.out.println(message);
        System.out.println(context.getId());
    }

    @Test
    public void mockingTest() {
        when(mockMeBean.mockMe()).thenReturn("mocked");
        System.out.println(mockMeBean.mockMe());
    }

    @Test
    public void mockingDependencyTest() {
        when(mockMeBean.mockMe()).thenReturn("mocked");
        System.out.println(injectMockExampleService.callMockMeBean());
    }

    @Test
    void matchersAndExpectations() {
        when(mockMeBean.mockMe()).thenReturn("test");
        System.out.println(mockMeBean.mockMe());
        when(mockMeBean.mockMeWithArguments(anyString())).thenReturn("withArg");
        System.out.println(mockMeBean.mockMeWithArguments(""));
        when(mockMeBean.mockMeWithArguments(eq("1"))).thenReturn("one");
        when(mockMeBean.mockMeWithArguments(eq("2"))).thenReturn("two");
        System.out.println(mockMeBean.mockMeWithArguments("1"));
        System.out.println(mockMeBean.mockMeWithArguments("2"));

        Mockito.reset(mockMeBean);
        mockMeBean.mockMe();
        verify(mockMeBean).mockMe();

        Mockito.reset(mockMeBean);
        mockMeBean.mockMeWithArguments("test");
        verify(mockMeBean).mockMeWithArguments("test");

        Mockito.reset(mockMeBean);
        mockMeBean.mockMe();
        mockMeBean.mockMe();
        verify(mockMeBean, times(2)).mockMe();

        Mockito.reset(mockMeBean);
        verifyNoInteractions(mockMeBean);
    }

    @Test
    void mockAndTestExceptions() {
        when(mockMeBean.mockMeWithArguments(null))
            .thenThrow(new IllegalArgumentException());
        assertThrows(
            IllegalArgumentException.class,
            () -> mockMeBean.mockMeWithArguments(null)
        );
    }
}
