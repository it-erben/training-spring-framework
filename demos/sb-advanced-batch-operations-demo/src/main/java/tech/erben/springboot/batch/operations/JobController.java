package tech.erben.springboot.batch.operations;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Betriebsschnittstelle des Dienstes. Sie bildet ab, was ein Betreiber
 * tatsächlich braucht: einen Job auslösen, sehen was läuft, eine
 * hängengebliebene Execution aufräumen und den Lauf wieder aufnehmen.
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final JobLockRepository jobLockRepository;
    private final OperationsProperties properties;
    private final Job orderImportJob;

    public JobController(JobOperator jobOperator,
                         JobRepository jobRepository,
                         JobLockRepository jobLockRepository,
                         OperationsProperties properties,
                         Job orderImportJob) {
        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.jobLockRepository = jobLockRepository;
        this.properties = properties;
        this.orderImportJob = orderImportJob;
    }

    /**
     * Startet den Import. {@code lauf} ist ein identifizierender Parameter:
     * Ein neuer Wert erzeugt eine neue JobInstance, derselbe Wert trifft die
     * alte wieder.
     */
    @PostMapping("/order-import")
    public ResponseEntity<Map<String, Object>> start(@RequestParam String lauf) {
        if (properties.lock() && !jobLockRepository.tryAcquire(OrderImportJobConfig.JOB_NAME)) {
            return conflict("Ein Lauf hält die Sperre");
        }
        JobParameters parameters = new JobParametersBuilder().addString("lauf", lauf).toJobParameters();
        try {
            JobExecution execution = jobOperator.start(orderImportJob, parameters);
            return ResponseEntity.accepted().body(describe(execution));
        }
        catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException refused) {
            jobLockRepository.release(OrderImportJobConfig.JOB_NAME);
            return conflict(refused.getClass().getSimpleName());
        }
        // Zwei wirklich gleichzeitige Starts derselben JobInstance kommen nicht
        // als saubere Batch-Ausnahme an: Beide Threads legen die JobInstance
        // an, einer verliert am Unique Constraint von BATCH_JOB_INSTANCE. Der
        // Schutz sitzt in der Datenbank, nicht in einer Prüfung davor.
        catch (DuplicateKeyException lostRace) {
            jobLockRepository.release(OrderImportJobConfig.JOB_NAME);
            return conflict("Wettlauf um dieselbe JobInstance am Unique Constraint verloren");
        }
        catch (Exception failed) {
            jobLockRepository.release(OrderImportJobConfig.JOB_NAME);
            throw new IllegalStateException(failed);
        }
    }

    @GetMapping("/running")
    public List<Map<String, Object>> running() {
        Set<JobExecution> executions = jobRepository.findRunningJobExecutions(OrderImportJobConfig.JOB_NAME);
        return executions.stream().map(this::describe).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> summary(@PathVariable long id) {
        return describe(requireExecution(id));
    }

    /**
     * Räumt eine Execution auf, die ein abgestürzter Prozess auf
     * {@code STARTED} zurückgelassen hat. Ohne diesen Schritt gilt der Lauf
     * als aktiv und blockiert jeden Neustart derselben JobInstance.
     */
    @PostMapping("/{id}/recover")
    public Map<String, Object> recover(@PathVariable long id) {
        return describe(jobOperator.recover(requireExecution(id)));
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<Map<String, Object>> restart(@PathVariable long id) throws Exception {
        JobExecution restarted = jobOperator.restart(requireExecution(id));
        return ResponseEntity.accepted().body(describe(restarted));
    }

    private JobExecution requireExecution(long id) {
        try {
            return jobRepository.getJobExecution(id);
        }
        catch (EmptyResultDataAccessException unknown) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keine Execution mit ID " + id);
        }
    }

    private Map<String, Object> describe(JobExecution execution) {
        return Map.of(
                "executionId", execution.getId(),
                "instanceId", execution.getJobInstance().getInstanceId(),
                "status", execution.getStatus().toString(),
                "parameters", execution.getJobParameters().toString(),
                "steps", execution.getStepExecutions().stream()
                        .map(step -> Map.of(
                                "step", step.getStepName(),
                                "read", step.getReadCount(),
                                "written", step.getWriteCount(),
                                "commits", step.getCommitCount()))
                        .toList());
    }

    private ResponseEntity<Map<String, Object>> conflict(String reason) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("abgewiesen", reason));
    }
}
