package lernen.orderapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.*;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class OrderImportService {
    private final JobOperator jobOperator;
    private final Job orderImportJob;
    private final JobRepository jobRepository;

    public Long fileImport(Path csvFile) throws JobInstanceAlreadyCompleteException,
            InvalidJobParametersException, JobExecutionAlreadyRunningException, JobRestartException {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("inputFile", csvFile.toAbsolutePath().toString())
                .toJobParameters();
        JobExecution jobex = jobOperator.start(orderImportJob, params);
        return jobex.getId();
    }

    public ExitStatus getOrderImportStatus( Long executionId) {
        JobExecution jobExecution = jobRepository.getJobExecution(executionId);
        if (jobExecution == null) {
            throw new JobExecutionNotFoundException(executionId);
        }
        return jobExecution.getExitStatus();
    }
}