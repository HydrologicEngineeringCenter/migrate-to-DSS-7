import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MigrateToDss7 {
    private static final Logger logger =java.util.logging.Logger.getLogger(MigrateToDss7.class.getName());


    public static void main(String[] args) {
        Options options = new Options();
        options.addOption( "d", "directory", true, "Migrate files in the specified directory.");

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse( options, args);

            if (cmd.hasOption("d")) {
                String directory = cmd.getOptionValue("d");
                Path directoryPath = Paths.get(directory);
                if (Files.notExists(directoryPath)){
                    logger.warning(() -> String.format("Directory %s does not exist", directory));
                    return;
                }

                Set<String> paths;
                try {
                    paths = Files.walk(directoryPath)
                            .filter(path -> path.endsWith(".dss"))
                            .map(Path::toString)
                            .collect(Collectors.toSet());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e, e::getMessage);
                    return;
                }

                BatchDss7Migrater migrater = BatchDss7Migrater.create(paths);
                migrater.migrate();

            }
        } catch (ParseException e) {
            logger.log(Level.SEVERE, e, e::getMessage);
        }

    }
}
