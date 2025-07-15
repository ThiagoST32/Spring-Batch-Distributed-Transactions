package com.trevisan.distributed_transactions;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class BatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    @Qualifier("transactionManagerJta")
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job myJob(Step step){
        return new JobBuilder("job01", jobRepository)
            .start(step)
            .incrementer(new RunIdIncrementer())
            .build();
    }

    @Bean
    public Step myStep (ItemReader<Pessoa> reader, ItemWriter<Pessoa> writer){
        return new StepBuilder("step01", jobRepository)
            .<Pessoa, Pessoa>chunk(200, transactionManager)
            .reader(reader)
            .writer(writer)
            .transactionManager(transactionManager)
            .build();
    }

    @Bean
    public ItemReader<Pessoa> reader() {
        return new FlatFileItemReaderBuilder<Pessoa>()
            .name("reader")
            .resource(new FileSystemResource("servers/data/cadastros.csv"))
            .comments("---")
            .delimited()
            .names("name","document","email","phone","age")
            .targetType(Pessoa.class)
            .build();
    }

    @Bean
    public ItemWriter<Pessoa>writer(@Qualifier("writerOnDb01") ItemWriter<Pessoa> writerOnDb01, @Qualifier("writerOnDb02") ItemWriter<Pessoa> writerONDb02){
        return new CompositeItemWriterBuilder<Pessoa>()
            .delegates(List.of(writerOnDb01, writerONDb02))
            .build();
    }

    @Bean
    public ItemWriter<Pessoa> writerOnDb01(@Qualifier("data01") DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Pessoa>()
            .dataSource(dataSource)
            .sql("INSERT INTO pessoa (name, document, email, phone, age) VALUES(:name, :document, :email, :phone, :age)")
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .build();
    }

    @Bean
    public ItemWriter<Pessoa> writerOnDb02(@Qualifier("data02") DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Pessoa>()
            .dataSource(dataSource)
            .sql("INSERT INTO pessoa (name, document, email, phone, age) VALUES(:name, :document, :email, :phone, :age)")
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .build();
    }
}
