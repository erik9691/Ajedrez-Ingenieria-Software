import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.nio.file.*;

class ArchitectureTest {
    @Test
    void coreDoesNotImportAdaptersOrPresentationInfrastructure() throws Exception {
        try (var sources = Files.walk(Path.of("src/main/core"))) {
            for (Path file : sources.filter(p -> p.toString().endsWith(".java")).toList()) {
                String code = Files.readString(file);
                for (String forbidden :
                        new String[] {
                            "import main.adapters.",
                            "import java.io.",
                            "import java.nio.file.",
                            "import java.awt.",
                            "import javax.swing.",
                            "System.out."
                        }) assertFalse(code.contains(forbidden), file + ": " + forbidden);
            }
        }
    }
}
