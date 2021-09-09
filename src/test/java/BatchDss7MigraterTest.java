import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BatchDss7MigraterTest {
    private static final Logger logger = Logger.getLogger(BatchDss7Migrater.class.getName());

    @Test
    void migrate() {
        String directory = "C:/Temp";
        Path directoryPath = Paths.get(directory);
        Set<String> paths;
        try {
            paths = Files.walk(directoryPath, 2)
                    .map(Path::toString)
                    .filter(s -> s.endsWith(".dss"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, e::getMessage);
            return;
        }

        BatchDss7Migrater migrater = BatchDss7Migrater.create(paths);
        migrater.migrate();
    }

}