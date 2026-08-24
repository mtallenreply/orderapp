package lernen.orderapp.batch;

import lernen.orderapp.entity.Order;
import lernen.orderapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@RequiredArgsConstructor
@Configuration

public class OrderImportJobConfig {
    private final JobRepository jobRepository;
    private final OrderImportInputProcessor processor;
    @Bean
    @StepScope
    public FlatFileItemReader<OrderImportZeile> orderReader(@Value("#{jobParameters['inputFile']}") String inputFile) {

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
    public RepositoryItemWriter<Order> orderWriter(OrderRepository orderRepository) {
        return new RepositoryItemWriterBuilder<Order>()
                .repository(orderRepository)
                .methodName("save")
                .build();
    }
    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor= new SimpleAsyncTaskExecutor("orderImport-");
        executor.setConcurrencyLimit(4);// ohne eventuell zu viele Threads
        return executor;
    }
    @Bean
    public Step importStep(FlatFileItemReader<OrderImportZeile> orderReader,RepositoryItemWriter<Order> orderWriter){

        return new StepBuilder("importStep", jobRepository)
                .<OrderImportZeile, Order>chunk(1)// definiert commit Größe hier gerade ineffizient
                .reader(new SynchronizedItemStreamReader<>(orderReader)) // Thread safety
                .processor(processor)
                .taskExecutor((AsyncTaskExecutor) taskExecutor())
                .writer(orderWriter)
                .faultTolerant()
                .build();
    }
    @Bean
    public Job orderImportJob(Step importStep) {
        return new JobBuilder("ImportJob", jobRepository)
                .start(importStep)
                .build();
    }


}
