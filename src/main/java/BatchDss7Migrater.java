import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class BatchDss7Migrater {
    private static final Logger logger = Logger.getLogger(BatchDss7Migrater.class.getName());

    Set<String> paths;

    private final PropertyChangeSupport support;

    private BatchDss7Migrater(Set<String> paths) {
        this.paths = paths;
        support = new PropertyChangeSupport(this);
    }

    public static BatchDss7Migrater create(Set<String> paths) {
        return new BatchDss7Migrater(paths);
    }

    public void migrate() {
        AtomicInteger count = new AtomicInteger();
        Instant start = Instant.now();
        paths.parallelStream().forEach(path -> {
            Dss7Migrater migrater = Dss7Migrater.create(path);
            migrater.addPropertyChangeListener(evt -> count.incrementAndGet());
            migrater.migrate();
        });
        Instant end = Instant.now();
        logger.info(() -> String.format("Migrated %s files in ", count.get())
                + Duration.between(start, end).toSeconds() + " seconds");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl){
        support.removePropertyChangeListener(pcl);
    }
}
