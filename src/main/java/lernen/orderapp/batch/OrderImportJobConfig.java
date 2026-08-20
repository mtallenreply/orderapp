package lernen.orderapp.batch;

import lernen.orderapp.entity.Order;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.RetryException;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class OrderImportJobConfig {

    @Bean
    public Step protokollStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("protokollStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Batch-Step läuft.");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }


    @Bean
    public Job orderImportJob(JobRepository jobRepository, Step importStep) {
        return new JobBuilder("ImportJob", jobRepository)
                .start(importStep)
                .build();
    }


    @Bean
    public FlatFileItemReader<OrderImportZeile> orderReader() {
        return new FlatFileItemReaderBuilder<OrderImportZeile>()
                .name("orderReader")
                .resource(new ClassPathResource("beispiel-bestellungen.csv"))
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names("orderId", "customerId", "customerName","productSku","quantity","unitPrice","orderDate","channel")
                .targetType(OrderImportZeile.class)
                .build();
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           FlatFileItemReader<OrderImportZeile> orderReader,
                           OrderImportInputProcessor processor, OrderImportWriter writer) {
        return new StepBuilder("importStep", jobRepository)
                .<OrderImportZeile, Order>chunk(1)
                .reader(orderReader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
//                .skip(FlatFileParseException.class)
//                .skip(RetryException.class)
//                .skip(ObjectOptimisticLockingFailureException.class)

                .build();
    }
}
