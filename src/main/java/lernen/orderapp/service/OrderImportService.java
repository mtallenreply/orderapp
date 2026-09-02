package lernen.orderapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public final class OrderImportService {
    private final JobOperator jobOperator;
    private final Job orderImportJob;
    private final JobRepository jobRepository;

    public Long fileImport(final Path csvFile) throws JobInstanceAlreadyCompleteException,
            InvalidJobParametersException, JobExecutionAlreadyRunningException, JobRestartException {
        final JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("inputFile", csvFile.toAbsolutePath().toString())
                .toJobParameters();
        final JobExecution jobex = jobOperator.start(orderImportJob, params);
        return jobex.getId();
    }

    public ExitStatus getOrderImportStatus(final Long executionId) {
        final JobExecution jobExecution = jobRepository.getJobExecution(executionId);
        if (jobExecution == null) {
            throw new JobExecutionNotFoundException(executionId);
        }
        return jobExecution.getExitStatus();
    }
}
