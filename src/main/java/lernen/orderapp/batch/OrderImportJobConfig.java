package lernen.orderapp.batch;

import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Configuration

public class OrderImportJobConfig {
    private final JobRepository jobRepository;
    private final OrderImportInputProcessor processor;
    @Bean
    @StepScope
    public FlatFileItemReader<OrderImportZeile> orderReader(@Value("#{jobParameters['inputFile']}") final String inputFile) {

        return new FlatFileItemReaderBuilder<OrderImportZeile>()
                .name("orderReader")
                .resource(new FileSystemResource(inputFile))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("orderId", "customerId", "customerName","productSku","quantity","unitPrice","orderDate","channel")
                .targetType(OrderImportZeile.class)
                .build();
}

    @Bean
    public RepositoryItemWriter<Order> orderWriter(final OrderRepository orderRepository) {
        return new RepositoryItemWriterBuilder<Order>()
                .repository(orderRepository)
                .methodName("save")
                .build();
    }
    @Bean
    public TaskExecutor taskExecutor() {
        final SimpleAsyncTaskExecutor executor= new SimpleAsyncTaskExecutor("orderImport-");
        executor.setConcurrencyLimit(4);// ohne eventuell zu viele Threads
        return executor;
    }
    @Bean
    public SkipListener<OrderImportZeile, Order> skipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInRead(final Throwable t) {
                log.warn("Zeile beim Lesen übersprungen: {}", t.getMessage());
            }

            @Override
            public void onSkipInProcess(final OrderImportZeile item, final Throwable t) {
                log.warn("Zeile beim Verarbeiten übersprungen ({}): {}", item, t.getMessage());
            }

            @Override
            public void onSkipInWrite(final Order item, final Throwable t) {
                log.warn("Order beim Schreiben übersprungen ({}): {}", item, t.getMessage());
            }
        };
    }
    @Bean
    public JobExecutionListener tempFileCleanUpListener() {
       return new JobExecutionListener() {
           @Override
           public void afterJob(@NonNull final JobExecution jobExecution){
               final String inputFile = jobExecution.getJobParameters().getString("inputFile");
               try {
                   Files.deleteIfExists(Path.of(Objects.requireNonNull(inputFile)));
               } catch (final IOException e) {
                   log.warn("Konnte Temp-Datei nicht löschen: {}", inputFile, e);
               }

           }
       };
    }
    @Bean
    public Step importStep(final FlatFileItemReader<OrderImportZeile> orderReader,
                           final RepositoryItemWriter<Order> orderWriter,
                           final SkipListener<OrderImportZeile, Order> skipListener){

        return new StepBuilder("importStep", jobRepository)
                .<OrderImportZeile, Order>chunk(1)// definiert commit Größe hier gerade ineffizient
                .reader(new SynchronizedItemStreamReader<>(orderReader)) // Thread safety
                .processor(processor)
                .taskExecutor((AsyncTaskExecutor) taskExecutor())
                .writer(orderWriter)
                .faultTolerant()// alleine nutzlos
                .skip(DataIntegrityViolationException.class)// nur hiermit
                .skip(FlatFileParseException.class)   // Reader: falsches CSV-Format/Typkonvertierung
                .skip(IllegalArgumentException.class) // Processor: ungültiger Channel-Wert oder Datum
                .skipLimit(5).listener(skipListener)// oder hiermit wertvoll
                .build();
    }
    @Bean
    public Job orderImportJob(final Step importStep) {
        return new JobBuilder("ImportJob", jobRepository)
                .start(importStep)
                .listener(tempFileCleanUpListener())
                .build();
    }


}
