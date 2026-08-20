package tech.erben.springboot.batch.operations;

import java.time.LocalDateTime;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Sperre gegen Doppelstarts, gebaut auf einem Unique Constraint. Das Prinzip
 * ist dasselbe, das einschlägige Bibliotheken verwenden: Wer die Zeile
 * schreiben kann, hält die Sperre. H2 kennt kein Advisory Lock, und in zwanzig
 * Zeilen bleibt sichtbar, worauf der Schutz beruht.
 *
 * <p>Was Spring Batch selbst abdeckt, ist die identische JobInstance. Zwei
 * Läufe mit verschiedenen Parametern hält es nicht auseinander — dafür ist
 * diese Tabelle da.
 */
@Repository
public class JobLockRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobLockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean tryAcquire(String jobName) {
        try {
            jdbcTemplate.update("insert into job_lock (job_name, acquired_at) values (?, ?)",
                    jobName, LocalDateTime.now());
            return true;
        }
        catch (DuplicateKeyException alreadyHeld) {
            return false;
        }
    }

    public void release(String jobName) {
        jdbcTemplate.update("delete from job_lock where job_name = ?", jobName);
    }
}
