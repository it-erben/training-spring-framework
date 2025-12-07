package tech.erben.springboot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public interface OperatingSystem {
    String writeOsInfo() throws IOException;

    class MacOperatingSystem implements OperatingSystem {

        @Override
        public String writeOsInfo() throws IOException {
            Process process = Runtime.getRuntime().exec("sw_vers");
            return printOsInfo(process);
        }
    }

    class LinuxOperatingSystem implements OperatingSystem {

        @Override
        public String writeOsInfo() throws IOException {
            Process process = Runtime.getRuntime().exec("lsb_release -a");
            return printOsInfo(process);
        }
    }

    class WindowsOperatingSystem implements OperatingSystem {

        @Override
        public String writeOsInfo() throws IOException {
            Process process = Runtime
                .getRuntime()
                .exec("systeminfo | findstr /B /C:\"OS Name\" /C:\"OS Version\"");
            return printOsInfo(process);
        }
    }

    private static String printOsInfo(Process process) throws IOException {
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            )
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                return line;
            }
        }
        return "";
    }
}
