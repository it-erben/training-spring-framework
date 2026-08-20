package tech.erben.springboot.batch.operations;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

/**
 * Gibt die Sperre frei, sobald der Job endet. Der Absturz aus Szenario 2 läuft
 * hier absichtlich nicht durch: Eine Sperre, die ein toter Prozess hält, ist
 * genau das Betriebsproblem, über das im Modul gesprochen wird.
 */
public class JobLockListener implements JobExecutionListener {

    private final JobLockRepository jobLockRepository;

    public JobLockListener(JobLockRepository jobLockRepository) {
        this.jobLockRepository = jobLockRepository;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        jobLockRepository.release(jobExecution.getJobInstance().getJobName());
    }
}
