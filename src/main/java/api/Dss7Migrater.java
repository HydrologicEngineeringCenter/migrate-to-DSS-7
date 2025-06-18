package api;

import hec.heclib.dss.HecDSSFileAccess;
import hec.heclib.dss.HecDSSFileDataManager;
import hec.heclib.dss.HecDSSUtilities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dss7Migrater {
    private static final Logger logger = Logger.getLogger(Dss7Migrater.class.getName());

    private final String fileName;
    private final PropertyChangeSupport support;

    private Dss7Migrater(String fileName){
        this.fileName = fileName;
        support = new PropertyChangeSupport(this);
    }

    public static Dss7Migrater create(String pathToFile) {
        return new Dss7Migrater(pathToFile);
    }

    public void migrate() {
        Path pathToFile = Paths.get(fileName);
        if (Files.notExists(pathToFile)) {
            logger.info(() -> String.format("File does not exist: %s", fileName));
            return;
        }

        HecDSSUtilities utilities = new HecDSSUtilities();
        utilities.setDSSFileName(fileName);

        if (utilities.getDssFileVersion() == 7) {
            utilities.close();
            Object[] args = {fileName};
            String message = MessageFormat.format("File is v7: {0}", args);
            support.firePropertyChange("Processed", null, message);
            return;
        }

        if (utilities.getDssFileVersion() == 6) {
            String timeString = String.valueOf(Instant.now().toEpochMilli());
            String tempFileName = filenameSansExt(fileName) +
                    "_" +
                    timeString +
                    ".dss";

            Path pathToTempFile = Path.of(tempFileName);

            int status = utilities.convertVersion(tempFileName);
            utilities.close();
            HecDSSFileAccess.closeAllFiles();

            if (status != 0) {
                Object[] args = {fileName};
                String message = MessageFormat.format("Failed: {0}", args);
                logger.info(() -> message);
                support.firePropertyChange("Processed", null, message);

                try {
                    Files.delete(pathToTempFile);
                    logger.info(() -> String.format("Deleted temporary file: %s", tempFileName));
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e, e::getMessage);
                }

                return;
            }

            HecDSSFileDataManager manager = new HecDSSFileDataManager();
            // Manually clear the cache or this file will still register as v6 on subsequent access
            manager.unmapFile(fileName);
            // Manually close the temp file that was created
            manager.closeFile(tempFileName);

            try {
                Files.delete(pathToFile);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, e::getMessage);
                return;
            }

            try {
                Files.move(pathToTempFile, pathToFile);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, e::getMessage);
                return;
            }

            Object[] args = {fileName};
            String message = MessageFormat.format("Migrated: {0}", args);
            logger.info(() -> message);
            support.firePropertyChange("Migrated", null, message);
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
