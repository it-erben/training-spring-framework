package erben.digital;

public class CustomService {

    private String message;

    public CustomService(String message) {
        this.message = message;
    }

    public void printMessage() {
        System.out.println(message);
    }
}
