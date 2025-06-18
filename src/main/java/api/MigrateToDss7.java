package api;

import hec.heclib.dss.HecDSSFileAccess;
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
        options.addOption("v", "version", true, "Print DSS version for a specified DSS file.");

        Option multiPaths = Option.builder("p")
                .longOpt("paths")
                .hasArgs() // unlimited args
                .desc("Path to files, take multiple space-separated values")
                .build();
        options.addOption(multiPaths);

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

            } else if (cmd.hasOption("p")) {
                Set<String> paths = Set.of(cmd.getOptionValues("p"));
                BatchDss7Migrater migrater = BatchDss7Migrater.create(paths);
                migrater.migrate();
            } else if (cmd.hasOption("v")) {
                String dssFilename = cmd.getOptionValue("v");
                int version = HecDSSFileAccess.getDssFileVersion(dssFilename);
                System.out.println(version);
            }
        } catch (ParseException e) {
            logger.log(Level.SEVERE, e, e::getMessage);
        }

    }
}
