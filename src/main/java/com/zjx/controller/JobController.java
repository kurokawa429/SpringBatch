package com.zjx.controller;

import com.zjx.service.IEmployeeService;
import lombok.Setter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class JobController {
    @Setter(onMethod_ = {@Autowired})
    private IEmployeeService employeeService;

    @Setter(onMethod_ = {@Autowired})
    private JobLauncher jobLauncher;

    @Setter(onMethod_ = {@Autowired})
    private JobExplorer jobExplorer;

    @Setter(onMethod_ = {@Autowired})
    @Qualifier("csvToDBJob")
    private Job csvToDBJob;

    @Setter(onMethod_ = {@Autowired})
    @Qualifier("dbToDBJob")
    private Job dbToDBJob;

    @GetMapping("/csvToDB")
    public String csvToDB() throws Exception {
        employeeService.truncateTemp(); //清空数据运行多次执行

        //需要多次执行，run.id 必须重写之前，再重构一个新的参数对象
        JobParameters jobParameters = new JobParametersBuilder(new JobParameters(),jobExplorer)
                .addLong("time", new Date().getTime())
                .getNextJobParameters(csvToDBJob).toJobParameters();
        JobExecution run = jobLauncher.run(csvToDBJob, jobParameters);
        return run.getId().toString();
    }

    @GetMapping("/dbToDB")
    public String dbToDB() throws Exception {
        employeeService.truncateAll();
        JobParameters jobParameters = new JobParametersBuilder(new JobParameters(),jobExplorer)
                .addLong("time", new Date().getTime())
                .getNextJobParameters(dbToDBJob).toJobParameters();
        JobExecution run = jobLauncher.run(dbToDBJob, jobParameters);
        return run.getId().toString();
    }
}
