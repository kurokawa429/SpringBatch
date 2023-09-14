package com.zjx.job.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;

public class CsvToDBJobListener {

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        long begin = System.currentTimeMillis();
        jobExecution.getExecutionContext().putLong("begin", begin);
        System.err.println("-------------------------【CsvToDBJob开始时间：】---->"+begin+"<-----------------------------");
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        long begin = jobExecution.getExecutionContext().getLong("begin");
        long end = System.currentTimeMillis();
        System.err.println("-------------------------【CsvToDBJob结束时间：】---->"+end+"<-----------------------------");
        System.err.println("-------------------------【CsvToDBJob总耗时：】---->"+(end - begin)+"<-----------------------------");
    }
}
