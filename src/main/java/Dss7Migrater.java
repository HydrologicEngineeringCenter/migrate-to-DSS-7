import hec.heclib.dss.HecDSSUtilities;
import hec.heclib.dss.HecDataManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dss7Migrater {
    private static final Logger logger = Logger.getLogger(Dss7Migrater.class.getName());

    private final String pathToFile;
    private final PropertyChangeSupport support;

    private Dss7Migrater(String pathToFile){
        this.pathToFile = pathToFile;
        support = new PropertyChangeSupport(this);
    }

    public static Dss7Migrater create(String pathToFile) {
        return new Dss7Migrater(pathToFile);
    }

    public void migrate() {
        Path filePath = Paths.get(pathToFile);
        if (Files.notExists(filePath)) {
            logger.info(() -> String.format("File does not exist: %s", pathToFile));
            return;
        }

        HecDSSUtilities utilities = new HecDSSUtilities();
        utilities.setDSSFileName(pathToFile);
        if (utilities.getDssFileVersion() == 7) {
            logger.info(() -> String.format("File is version 7: %s", pathToFile));
        }

        if (utilities.getDssFileVersion() == 6) {
            String timeString = String.valueOf(Instant.now().toEpochMilli());
            String pathToTempFile = filenameSansExt(pathToFile) +
                    "_" +
                    timeString +
                    ".dss";

            int recordCount = utilities.getNumberRecords();
            if (recordCount == 0) {
                utilities.closeDSSFile();
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e, e::getMessage);
                }

                HecDataManager manager = new HecDataManager(pathToFile);
                manager.open();
                manager.close();
                logger.info(() -> String.format("Migrated file: %s", pathToFile));
                support.firePropertyChange("fileMigrated", pathToFile, pathToFile);
                return;
            }

            int status = utilities.convertVersion(pathToTempFile);
            if (status != 0) {
                logger.info(() -> String.format("Failed to convert file %s", pathToFile));
                utilities.closeDSSFile();

                try {
                    Files.delete(Path.of(pathToTempFile));
                    logger.info(() -> String.format("Deleted temporary file %s", pathToTempFile));
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e, e::getMessage);
                }

                return;
            }

            utilities.closeDSSFile();

            try {
                Files.delete(filePath);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, e::getMessage);
                return;
            }

            try {
                Files.move(Paths.get(pathToTempFile), filePath);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, e::getMessage);
                return;
            }

            logger.info(() -> String.format("Migrated file: %s", pathToFile));
            support.firePropertyChange("fileMigrated", pathToFile, pathToFile);

        }

    }

    private static String filenameSansExt(String filename) {
        return filename.replaceFirst("[.][^.]+$", "");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl){
        support.removePropertyChangeListener(pcl);
    }
}
