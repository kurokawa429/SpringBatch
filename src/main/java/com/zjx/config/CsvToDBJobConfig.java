package com.zjx.config;

import com.zjx.entity.Employee;
import com.zjx.job.listener.CsvToDBJobListener;
import lombok.Setter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.io.File;

@Configuration
public class CsvToDBJobConfig {

    @Setter(onMethod_ = {@Autowired})
    private JobBuilderFactory jobBuilderFactory;

    @Setter(onMethod_ = {@Autowired})
    private StepBuilderFactory stepBuilderFactory;

    @Setter(onMethod_ = {@Autowired})
    private SqlSessionFactory sqlSessionFactory;

    @Value("${job.data.path}")
    private String path;

    //多线程读-读文件，使用FlatFileItemReader
    @Bean
    public FlatFileItemReader<Employee> cvsToDBItemReader(){
        return new FlatFileItemReaderBuilder<Employee>()
                .name("employeeCSVItemReader")
                .saveState(false) //防止状态被覆盖
                .resource(new PathResource(new File(path, "employee.csv").getAbsolutePath()))
                .delimited()
                .names("id", "name", "age", "sex")
                .targetType(Employee.class)
                .build();
    }

    //数据库写-使用mybatis提供批处理读入
    @Bean
    public MyBatisBatchItemWriter<Employee> cvsToDBItemWriter(){
        MyBatisBatchItemWriter<Employee> itemWriter = new MyBatisBatchItemWriter<>();
        itemWriter.setSqlSessionFactory(sqlSessionFactory); //需要指定sqlsession工厂
        //指定要操作sql语句，路径id为：EmployeeMapper.xml定义的sql语句id
        itemWriter.setStatementId("com.zjx.mapper.EmployeeMapper.saveTemp");  //操作sql
        return itemWriter;
    }

    @Bean
    public Step csvToDBStep(){
        return stepBuilderFactory.get("csvToDBStep")
                .<Employee, Employee>chunk(10000)  //每个块10000个 共50个
                .reader(cvsToDBItemReader())
                .writer(cvsToDBItemWriter())
                .taskExecutor(new SimpleAsyncTaskExecutor())  //多线程读写
                .build();

    }

    //job监听器
    @Bean
    public CsvToDBJobListener csvToDBJobListener(){
        return new CsvToDBJobListener();
    }

    @Bean
    public Job csvToDBJob(){
        return jobBuilderFactory.get("csvToDB-step-job-test")
                .start(csvToDBStep())
                .incrementer(new RunIdIncrementer()) //保证可以多次执行
                .listener(csvToDBJobListener())
                .build();

    }
}
