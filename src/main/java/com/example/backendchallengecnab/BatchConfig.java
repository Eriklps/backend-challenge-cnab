package com.example.backendchallengecnab;

import com.example.backendchallengecnab.entity.Transacao;
import com.example.backendchallengecnab.entity.TransacaoCNAB;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.backendchallengecnab.entity.Transacao;
import com.example.backendchallengecnab.entity.TransacaoCNAB;
import com.example.backendchallengecnab.entity.TipoTransacao;

import java.math.BigDecimal;

import javax.sql.DataSource;

@Configuration
public class BatchConfig {
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    public BatchConfig(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
    }

    @Bean
    Job job(Step step) {
        return new JobBuilder("job", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    Step step(
            FlatFileItemReader<TransacaoCNAB> reader,
            ItemProcessor<TransacaoCNAB, Transacao> processor,
            ItemWriter<Transacao> writer) {
        return new StepBuilder("step", jobRepository)
                .<TransacaoCNAB, Transacao>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // READER
    @StepScope
    @Bean
    FlatFileItemReader<TransacaoCNAB> reader(
            @Value("#{jobParameters['cnabFile']}") Resource resource) {
        return new FlatFileItemReaderBuilder<TransacaoCNAB>()
                .name("reader")
                .resource(new FileSystemResource("files/CNAB.txt"))
                .fixedLength()
                .columns(
                        new Range(1, 1), new Range(2, 9),
                        new Range(10, 19), new Range(20, 30),
                        new Range(31, 42), new Range(43, 48),
                        new Range(49, 62), new Range(63, 80)
                )
                .names(
                        "tipo", "data", "valor", "cpf",
                        "cartao", "hora", "donoDaLoja", "nomeDaLoja"
                )
                .targetType(TransacaoCNAB.class)
                .build();
    }

    // PROCESSOR
    @Bean
    ItemProcessor<TransacaoCNAB, Transacao> processor() {
        return item -> {
            var tipoTransacao = TipoTransacao.findByTipo(item.tipo());
            var valorNormalizado = item.valor()
                    .divide(new BigDecimal(100))
                    .multiply(tipoTransacao.getSinal());

            // Wither Pattern
            var transacao = new Transacao(
                    null, item.tipo(), null,
                    valorNormalizado,
                    item.cpf(), item.cartao(), null,
                    item.donoDaLoja().trim(), item.nomeDaLoja().trim())
                    .withData(item.data())
                    .withHora(item.hora());

            return transacao;
        };
    }

    // WRITER
    @Bean
    JdbcBatchItemWriter<Transacao> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transacao>()
                .dataSource(dataSource)
                .sql("""
              INSERT INTO transacao (
                tipo, data, valor, cpf, cartao,
                hora, dono_loja, nome_loja
              ) VALUES (
                :tipo, :data, :valor, :cpf, :cartao,
                :hora, :donoDaLoja, :nomeDaLoja
              )
            """)
                .beanMapped()
                .build();
    }

    @Bean
    JobLauncher jobLauncherAsync(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

}
