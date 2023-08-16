# 一、课程目标

## 课程目标

- 系统了解Spring Batch批处理

- 项目中能熟练使用Spring Batch批处理

  

## 课程内容

## 前置知识

- Java基础
- Maven

- Spring   SpringMVC   SpringBoot
- MyBatis

## 适合人群

- 想学习的所有人



# 二、Spring Batch简介

## 2.1 何为批处理？

何为批处理，大白话：就是将数据分批次进行处理的过程。比如：银行对账逻辑，跨系统数据同步等。

常规的批处理操作步骤：**系统A从数据库中导出数据到文件，系统B读取文件数据并写入到数据库**

*![image-20220914225838654](images/image-20220914225838654.png)*

典型批处理特点：

- 自动执行，根据系统设定的工作步骤自动完成

- 数据量大，少则百万，多则上千万甚至上亿。(如果是10亿，100亿那只能上大数据了)

- 定时执行，比如：每天，每周，每月执行。

  

## 2.2 Spring Batch了解

官网介绍：https://docs.spring.io/spring-batch/docs/current/reference/html/spring-batch-intro.html#spring-batch-intro

这里挑重点讲下：

- Sping Batch 是一个轻量级的、完善的的批处理框架，旨在帮助企业建立健壮、高效的批处理应用。

- Spring Batch 是Spring的一个子项目，基于Spring框架为基础的开发的框架
- Spring Batch 提供大量可重用的组件，比如：日志，追踪，事务，任务作业统计，任务重启，跳过，重复，资源管理等
- Spring Batch 是一个批处理应用框架，不提供调度框架，如果需要定时处理需要额外引入-调度框架，比如： Quartz



## 2.3 Spring Batch 优势

Spring Batch 框架通过提供丰富的开箱即用的组件和高可靠性、高扩展性的能力，使得开发批处理应用的人员专注于业务处理，提高处理应用的开发能力。下面就是使用Spring Batch后能获取到优势：

- 丰富的开箱即用组件

- 面向Chunk的处理

- 事务管理能力

- 元数据管理

- 易监控的批处理应用

- 丰富的流程定义

- 健壮的批处理应用

- 易扩展的批处理应用

- 复用企业现有的IT代码

  

## 2.4 Spring Batch 架构

Spring Batch 核心架构分三层：应用层，核心层，基础架构层。

*![image-20220915131050999](images/image-20220915131050999.png)*

**Application**：应用层，包含所有的批处理作业，程序员自定义代码实现逻辑。

**Batch Core**：核心层，包含Spring Batch启动和控制所需要的核心类，比如：JobLauncher， Job，Step等。

**Batch Infrastructure**：基础架构层，提供通用的读，写与服务处理。

三层体系使得Spring Batch 架构可以在不同层面进行扩展，避免影响，实现高内聚低耦合设计。



# 三、入门案例

## 3.1 批量处理流程

前面对Spring Batch 有大体了解之后，那么开始写个案例玩一下。

开始前，先了解一下Spring Batch程序运行大纲：

*![image-20220915132446839](images/image-20220915132446839.png)*

**JobLauncher**：作业调度器，作业启动主要入口。

**Job**：作业，需要执行的任务逻辑，

**Step**：作业步骤，一个Job作业由1个或者多个Step组成，完成所有Step操作，一个完整Job才算执行结束。

**ItemReader**：Step步骤执行过程中数据输入。可以从数据源(文件系统，数据库，队列等)中读取Item(数据记录)。

**ItemWriter**：Step步骤执行过程中数据输出，将Item(数据记录)写入数据源(文件系统，数据库，队列等)。

**ItemProcessor**：Item数据加工逻辑(输入)，比如：数据清洗，数据转换，数据过滤，数据校验等

**JobRepository**： 保存Job或者检索Job的信息。SpringBatch需要持久化Job(可以选择数据库/内存)，JobRepository就是持久化的接口



## 3.2 入门案例-H2版(内存)

**需求：打印一个hello spring  batch！不带读/写/处理**

**步骤1：导入依赖**

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.3</version>
    <relativePath/>
</parent>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

	<!--内存版-->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>

</dependencies>
```

其中的h2是一个嵌入式内存数据库，后续可以使用MySQL替换

**步骤2：创建测试方法**

```java
package com.langfeiyes.batch._01_hello;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class HelloJob {
    //job调度器
    @Autowired
    private JobLauncher jobLauncher;
    //job构造器工厂
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    //step构造器工厂
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    //任务-step执行逻辑由tasklet完成
    @Bean
    public Tasklet tasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello SpringBatch....");
                return RepeatStatus.FINISHED;
            }
        };
    }
    //作业步骤-不带读/写/处理
    @Bean
    public Step step1(){
        return stepBuilderFactory.get("step1")
                .tasklet(tasklet())
                .build();
    }
    //定义作业
    @Bean
    public Job job(){
        return jobBuilderFactory.get("hello-job")
                .start(step1())
                .build();
    }
    public static void main(String[] args) {
        SpringApplication.run(HelloJob.class, args);
    }

}

```

**步骤3：分析**

例子是一个简单的SpringBatch 入门案例，使用了最简单的一种步骤处理模型：Tasklet模型，step1中没有带上读/写/处理逻辑，只有简单打印操作，后续随学习深入，我们再讲解更复杂化模型。



## 3.3 入门案例-MySQL版

MySQL跟上面的h2一样，区别在连接数据库不一致。

**步骤1：在H2版本基础上导入MySQL依赖**

```xml
<!-- <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency> -->

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.12</version>
</dependency>
```

**步骤2：配置数据库四要素与初始化SQL脚本**

```yaml
spring:
  datasource:
    username: root
    password: admin
    url: jdbc:mysql://127.0.0.1:3306/springbatch?serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 初始化数据库，文件在依赖jar包中
  sql:
    init:
      schema-locations: classpath:org/springframework/batch/core/schema-mysql.sql
      mode: always
      #mode: never
```

这里要注意， sql.init.model 第一次启动为always， 后面启动需要改为never，否则每次执行SQL都会异常。

第一次启动会自动执行指定的脚本，后续不需要再初始化

![image-20220919221902905](images/image-20220919221902905.png)

**步骤3：测试**

跟H2版一样。



# 四、入门案例解析

**1>@EnableBatchProcessing**

批处理启动注解，要求贴配置类或者启动类上

```java
@SpringBootApplication
@EnableBatchProcessing
public class HelloJob {
    ...
}
```

贴上@EnableBatchProcessing注解后，SpringBoot会自动加载JobLauncher  JobBuilderFactory  StepBuilderFactory 类并创建对象交给容器管理，要使用时，直接@Autowired即可

```java
//job调度器
@Autowired
private JobLauncher jobLauncher;
//job构造器工厂
@Autowired
private JobBuilderFactory jobBuilderFactory;
//step构造器工厂
@Autowired
private StepBuilderFactory stepBuilderFactory;
```

**2>配置数据库四要素**

批处理允许重复执行，异常重试，此时需要保存批处理状态与数据，Spring Batch 将数据缓存在H2内存中或者缓存在指定数据库中。入门案例如果要保存在MySQL中，所以需要配置数据库四要素。

**3>创建Tasklet对象**

```java
//任务-step执行逻辑由tasklet完成
@Bean
public Tasklet tasklet(){
    return new Tasklet() {
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            System.out.println("Hello SpringBatch....");
            return RepeatStatus.FINISHED;
        }
    };
}
```

Tasklet负责批处理step步骤中具体业务执行，它是一个接口，有且只有一个execute方法，用于定制step执行逻辑。

```java
public interface Tasklet {
	RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception;
}
```

execute方法返回值是一个状态枚举类：RepeatStatus，里面有可继续执行态与已经完成态

```java
public enum RepeatStatus {
	/**
	 * 可继续执行的-tasklet返回这个状态会进入死循环
	 */
	CONTINUABLE(true), 
	/**
	 * 已经完成态
	 */
	FINISHED(false);
    ....
}
```

**4>创建Step对象**

```java
//作业步骤-不带读/写/处理
@Bean
public Step step1(){
    return stepBuilderFactory.get("step1")
        .tasklet(tasklet())
        .build();
}
```

Job作业执行靠Step步骤执行，入门案例选用最简单的Tasklet模式，后续再讲Chunk块处理模式。

**5>创建Job并执行Job**

```java
//定义作业
@Bean
public Job job(){
    return jobBuilderFactory.get("hello-job")
        .start(step1())
        .build();
}
```

创建Job对象交给容器管理，当springboot启动之后，会自动去从容器中加载Job对象，并将Job对象交给JobLauncherApplicationRunner类，再借助JobLauncher类实现job执行。

验证过程；

打断点，debug模式启动

*![image-20221130145001348](images/image-20221130145001348.png)*

**SpringApplication类run方法**

*![image-20221130145052497](images/image-20221130145052497.png)*

*![image-20221130145151076](images/image-20221130145151076.png)*

*![image-20221130145402058](images/image-20221130145402058.png)*

**JobLauncherApplicationRunner类**

*![image-20221130145444033](images/image-20221130145444033.png)*

*![image-20221130145514599](images/image-20221130145514599.png)*

*![image-20221130145601076](images/image-20221130145601076.png)*

*![image-20221130145621098](images/image-20221130145621098.png)*

**JobLauncher接口--实现类：SimpleJobLauncher**

*![image-20221130145748548](images/image-20221130145748548.png)*



# 五、作业对象 Job

## 5.1 作业介绍

### 5.1.1 作业定义

Job作业可以简单理解为一段业务流程的实现，可以根据业务逻辑拆分一个或者多个逻辑块(step)，然后业务逻辑顺序，逐一执行。

所以作业可以定义为：**能从头到尾独立执行的有序的步骤(Step)列表。**

- 有序的步骤列表

  一次作业由不同的步骤组成，这些步骤顺序是有意义的，如果不按照顺序执行，会引起逻辑混乱，比如购物结算，先点结算，再支付，最后物流，如果反过来那就乱套了，作业也是这么一回事。

- 从头到尾

  一次作业步骤固定了，在没有外部交互情况下，会从头到尾执行，前一个步骤做完才会到后一个步骤执行，不允许随意跳转，但是可以按照一定逻辑跳转。

- 独立

  每一个批处理作业都应该不受外部依赖影响情况下执行。

看回这幅图，批处理作业Job是由一组步骤Step对象组成，每一个作业都有自己名称，可以定义Step执行顺序。

*![image-20220915132446839](images/image-20220915132446839.png)*

### 5.1.2 作业代码设计

前面定义讲了作业执行是相互独立的，代码该怎么设计才能保证每次作业独立的性呢？

答案是：**Job instance**(作业实例) 与 **Job Execution**(作业执行对象)

**Job instance**(作业实例)

当作业运行时，会创建一个Job Instance(作业实例)，它代表作业的一次逻辑运行，可通过作业名称与作业标识参数进行区分。

比如一个业务需求： 每天定期数据同步，**作业名称-daily-sync-job**    **作业标记参数-当天时间**

**Job Execution**(作业执行对象)

当作业运行时，也会创建一个Job Execution(作业执行器)，负责记录Job执行情况(比如：开始执行时间，结束时间，处理状态等)。

*![三者间的关系](images/job-heirarchy.png)*

那为啥会出现上面架构设计呢？原因：批处理执行过程中可能出现两种情况：

- 一种是一次成功

  仅一次就成从头到尾正常执行完毕，在数据库中会记录一条Job Instance 信息， 跟一条 Job Execution 信息

- 另外一种异常执行

  在执行过程因异常导致作业结束，在数据库中会记录一条Job Instance 信息， 跟一条Job Execution 信息。如果此时使用相同识别参数再次启动作业，那么数据库中不会多一条Job Instance 信息， 但是会多了一条Job Execution 信息，这就意味中任务重复执行了。刚刚说每天批处理任务案例，如果当天执行出异常，那么人工干预修复之后，可以再次执行。

  

最后来个总结：

**Job Instance  =  Job名称  +  识别参数**

**Job Instance 一次执行创建一个 Job Execution对象**

**完整的一次Job Instance 执行可能创建一个Job Execution对象，也可能创建多个Job  Execution对象**



## 5.2 作业配置

再看回入门案例

```java
package com.langfeiyes.batch._01_hello;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class HelloJob {
    //job构造器工厂
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    //step构造器工厂
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    //任务-step执行逻辑由tasklet完成
    @Bean
    public Tasklet tasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello SpringBatch....");
                return RepeatStatus.FINISHED;
            }
        };
    }
    //作业步骤-不带读/写/处理
    @Bean
    public Step step1(){
        return stepBuilderFactory.get("step1")
                .tasklet(tasklet())
                .build();
    }
    //定义作业
    @Bean
    public Job job(){
        return jobBuilderFactory.get("hello-job")
                .start(step1())
                .build();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(HelloJob.class, args);
    }

}

```

在启动类中贴上@EnableBatchProcessing注解，SpringBoot会自动听JobLauncher  JobBuilderFactory  StepBuilderFactory 对象，分别用于执行Jog，创建Job，创建Step逻辑。有了这些逻辑，Job批处理就剩下组装了。



## 5.3 作业参数

### 5.3.1 JobParameters

前面提到，作业的启动条件是作业名称 + 识别参数，Spring Batch使用**JobParameters**类来封装了所有传给作业参数。

我们看下JobParameters 源码

```java
public class JobParameters implements Serializable {

	private final Map<String,JobParameter> parameters;

	public JobParameters() {
		this.parameters = new LinkedHashMap<>();
	}

	public JobParameters(Map<String,JobParameter> parameters) {
		this.parameters = new LinkedHashMap<>(parameters);
	}
    .....
}
```

*![image-20221127174012352](images/image-20221127174012352.png)*

从上面代码/截图来看，JobParameters 类底层维护了Map<String,JobParameter>，是一个Map集合的封装器，提供了不同类型的get操作。

### 5.3.2 作业参数设置

还记得Spring Batch 入门案例吗，当初debug时候看到Job作业最终是调用时 **JobLauncher **(job启动器)接口run方法启动。

看下源码：JobLauncher

```java
public interface JobLauncher {
	public JobExecution run(Job job, JobParameters jobParameters) throws JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException;

}
```

在JobLauncher 启动器执行run方法时，直接传入即可。

```java
jobLauncher.run(job, params);
```

那我们使用SpringBoot 方式启动Spring Batch该怎么传值呢？

**1>定义ParamJob类，准备好要执行的job**

```java
package com.langfeiyes.batch._02_params;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableBatchProcessing
public class ParamJob {
    //job构造器工厂
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    //step构造器工厂
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet tasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("param SpringBatch....");
                return RepeatStatus.FINISHED;
            }
        };
    }
    @Bean
    public Step  step1(){
        return  stepBuilderFactory.get("step1")
                .tasklet(tasklet())
                .build();
    }
    @Bean
    public Job job(){
        return jobBuilderFactory.get("param-job")
                .start(step1())
                .build();
    }
    public static void main(String[] args) {
        SpringApplication.run(HelloJob.class, args);
    }
}
```

**2>使用idea的命令传值的方式设置job作业参数**

**![image-20221130154519119](images/image-20221130154519119.png)**

>  **注意：如果不想这么麻烦，其实也可以，先空参数执行一次，然后指定参数后再执行。**

点击绿色按钮，启动SpringBoot程序，作业运行之后，会在batch_job_execution_params 增加一条记录，用于区分唯一的Job Instance实例

*![image-20221127175249374](images/image-20221127175249374.png)*



**注意：如果不改动JobParameters 参数内容，再执行一次批处理，会直接报错。**

*![image-20221127175635802](images/image-20221127175635802.png)*

**原因：Spring Batch 相同Job名与相同标识参数只能成功执行一次。**



### 5.3.3 作业参数获取

当将作业参数传入到作业流程，该如何获取呢？

*![image-20221130154813539](images/image-20221130154813539.png)*



Spring Batch 提供了2种方案：

**方案1：使用ChunkContext类**

ParamJob类中tasklet写法

```java
@Bean
public Tasklet tasklet(){
    return new Tasklet() {
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();
            System.out.println("params---name:" + parameters.get("name"));
            return RepeatStatus.FINISHED;
        }
    };
}
```

**注意：job名：param-job    job参数：name=dafei 已经执行了，再执行会报错**

**所以要么改名字，要么改参数，这里选择改job名字（拷贝一份job实例方法，然后注释掉，修改Job名称）**

```java
//    @Bean
//    public Job job(){
//        return jobBuilderFactory.get("param-job")
//                .start(step1())
//                .build();
//    }

    @Bean
    public Job job(){
        return jobBuilderFactory.get("param-chunk-job")
                .start(step1())
                .build();
    }
```



**方案2：使用@Value 延时获取**

```java
@StepScope
@Bean
public Tasklet tasklet(@Value("#{jobParameters['name']}")String name){
    return new Tasklet() {
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
            System.out.println("params---name:" + name);
            return RepeatStatus.FINISHED;
        }
    };
}

@Bean
public Step  step1(){
    return  stepBuilderFactory.get("step1")
        .tasklet(tasklet(null))
        .build();
}
```

step1调用tasklet实例方法时不需要传任何参数，Spring Boot 在加载Tasklet Bean实例时会自动注入。

```java
//    @Bean
//    public Job job(){
//        return jobBuilderFactory.get("param-chunk-job")
//                .start(step1())
//                .build();
//    }

@Bean
public Job job(){
    return jobBuilderFactory.get("param-value-job")
        .start(step1())
        .build();
}
```

这里要注意，**必须贴上@StepScope** ，表示在启动项目的时候，不加载该Step步骤bean，等step1()被调用时才加载。这就是所谓延时获取。



### 5.3.4 作业参数校验

当外部传入的参数进入作业时，如何确保参数符合期望呢？使用Spring Batch 的参数校验器：**JobParametersValidator** 接口。

先来看下JobParametersValidator 接口源码：

```java
public interface JobParametersValidator {
	void validate(@Nullable JobParameters parameters) throws JobParametersInvalidException;
}
```

JobParametersValidator 接口有且仅有唯一的validate方法，参数为JobParameters，没有返回值。这就意味着不符合参数要求，需要抛出异常来结束步骤。

#### 定制参数校验器

Spring Batch 提供JobParametersValidator参数校验接口，其目的就是让我们通过实现接口方式定制参数校验逻辑。

**需求：如果传入作业的参数name值 为null 或者 "" 时报错**

```java
public class NameParamValidator  implements JobParametersValidator {
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String name = parameters.getString("name");

        if(!StringUtils.hasText(name)){
            throw new JobParametersInvalidException("name 参数不能为空");
        }
    }
}
```

其中的JobParametersInvalidException 异常是Spring Batch 专门提供参数校验失败异常，当然我们也可以自定义或使用其他异常。

```java
package com.langfeiyes.batch._03_param_validator;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@SpringBootApplication
@EnableBatchProcessing
public class ParamValidatorJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet tasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();
                System.out.println("params---name:" + parameters.get("name"));
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    public Step  step1(){
        return  stepBuilderFactory.get("step1")
                .tasklet(tasklet())
                .build();
    }

    //配置name参数校验器
    @Bean
    public NameParamValidator validator(){
        return new NameParamValidator();
    }

    @Bean
    public Job job(){
        return jobBuilderFactory.get("name-param-validator-job")
                .start(step1())
                .validator(validator())  //参数校验器
                .build();
    }
    public static void main(String[] args) {
        SpringApplication.run(ParamValidatorJob.class, args);
    }
}

```

新定义**validator()**实例方法，将定制的参数解析器加到Spring容器中，修改job()实例方法，加上**.validator(validator())**   校验逻辑。

第一次启动时，没有传任何参数

```java
String name = parameters.getString("name");
```

name为null，直接报错

![image-20221129123520596](images/image-20221129123520596.png)

加上name=dafei参数之后，正常执行

*![image-20221130161208827](images/image-20221130161208827.png)*



#### 默认参数校验器

除去上面的定制参数校验器外，Spring Batch 也提供2个默认参数校验器：DefaultJobParametersValidator(默认参数校验器) 跟 CompositeJobParametersValidator(组合参数校验器)。

DefaultJobParametersValidator参数校验器

```java
public class DefaultJobParametersValidator implements JobParametersValidator, InitializingBean {
	private Collection<String> requiredKeys;
	private Collection<String> optionalKeys;
    ....
}   
```

默认的参数校验器它功能相对简单，维护2个key集合requiredKeys 跟 optionalKeys

- requiredKeys  是一个集合，表示作业参数jobParameters中必须包含集合中指定的keys
- optionalKeys  也是一个集合，该集合中的key 是可选参数



**需求：如果作业参数没有name参数报错，age参数可有可无**

```java
package com.langfeiyes.batch._03_param_validator;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@SpringBootApplication
@EnableBatchProcessing
public class ParamValidatorJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet tasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();
                System.out.println("params---name:" + parameters.get("name"));
                System.out.println("params---age:" + parameters.get("age"));
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    public Step  step1(){
        return  stepBuilderFactory.get("step1")
                .tasklet(tasklet())
                .build();
    }

    //配置name参数校验器
    @Bean
    public NameParamValidator validator(){
        return new NameParamValidator();
    }

     //配置默认参数校验器
    @Bean
    public DefaultJobParametersValidator defaultValidator(){
        DefaultJobParametersValidator defaultValidator = new DefaultJobParametersValidator();
        defaultValidator.setRequiredKeys(new String[]{"name"});  //必填
        defaultValidator.setOptionalKeys(new String[]{"age"});   //可选
        return defaultValidator;
    }

    @Bean
    public Job job(){
        return jobBuilderFactory.get("default-param-validator-job")
                .start(step1())
                //.validator(validator())  //参数校验器
                .validator(defaultValidator())  //默认参数校验器
                .build();
    }
    public static void main(String[] args) {
        SpringApplication.run(ParamValidatorJob.class, args);
    }
}

```

新定义defaultValidator() 实例方法，将默认参数解析器加到Spring容器中，修改job实例方法，加上**.validator(defaultValidator())。** 

右键启动，不填name 跟 age 参数，直接报错

![image-20221129123650952](images/image-20221129123650952.png)

如果填上name参数，即使不填age参数，可以通过，原因是age是可选的。

*![image-20221130162132720](images/image-20221130162132720.png)*



#### 组合参数校验器

CompositeJobParametersValidator 组合参数校验器，顾名思义就是将多个参数校验器组合在一起。

看源码，大体能看出该校验器逻辑

```java
public class CompositeJobParametersValidator implements JobParametersValidator, InitializingBean {

	private List<JobParametersValidator> validators;

	@Override
	public void validate(@Nullable JobParameters parameters) throws JobParametersInvalidException {
		for (JobParametersValidator validator : validators) {
			validator.validate(parameters);
		}
	}
	
	public void setValidators(List<JobParametersValidator> validators) {
		this.validators = validators;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(validators, "The 'validators' may not be null");
		Assert.notEmpty(validators, "The 'validators' may not be empty");
	}
}
```

底层维护一个validators 集合，校验时调用validate 方法，依次执行校验器集合中校验器方法。另外，多了一个afterPropertiesSet方法，用于校验validators 集合中的校验器是否为null。

**需求：要求步骤中必须有name属性，并且不能为空**

分析：必须有，使用DefaultJobParametersValidator 参数校验器， 不能为null，使用指定定义的NameParamValidator参数校验器

```java
package com.langfeiyes.batch._03_param_validator;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Map;

@SpringBootApplication
@EnableBatchProcessing
public class ParamValidatorJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet tasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();
                System.out.println("params---name:" + parameters.get("name"));
                System.out.println("params---age:" + parameters.get("age"));
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    public Step  step1(){
        return  stepBuilderFactory.get("step1")
                .tasklet(tasklet())
                .build();
    }

    //配置name参数校验器
    @Bean
    public NameParamValidator validator(){
        return new NameParamValidator();
    }

     //配置默认参数校验器
    @Bean
    public DefaultJobParametersValidator defaultValidator(){
        DefaultJobParametersValidator defaultValidator = new DefaultJobParametersValidator();
        defaultValidator.setRequiredKeys(new String[]{"name"});  //必填
        defaultValidator.setOptionalKeys(new String[]{"age"});   //可选
        return defaultValidator;
    }

    //配置组合参数校验器
    @Bean
    public CompositeJobParametersValidator compositeValidator(){

        DefaultJobParametersValidator defaultValidator = new DefaultJobParametersValidator();
        defaultValidator.setRequiredKeys(new String[]{"name"});  //name必填
        defaultValidator.setOptionalKeys(new String[]{"age"});   //age可选

        NameParamValidator nameParamValidator = new NameParamValidator();  //name 不能为空

        CompositeJobParametersValidator compositeValidator = new CompositeJobParametersValidator();
        //按照传入的顺序，先执行defaultValidator 后执行nameParamValidator
        compositeValidator.setValidators(Arrays.asList(defaultValidator, nameParamValidator));

        try {
            compositeValidator.afterPropertiesSet();  //判断校验器是否为null
        } catch (Exception e) {
            e.printStackTrace();
        }

        return compositeValidator;
    }

    @Bean
    public Job job(){
        return jobBuilderFactory.get("composite-param-validator-job")
                .start(step1())
                //.validator(validator())  //参数校验器
                //.validator(defaultValidator())  //默认参数校验器
                .validator(compositeValidator())  //组合参数校验器
                .build();
    }
    public static void main(String[] args) {
        SpringApplication.run(ParamValidatorJob.class, args);
    }
}

```

新定义compositeValidator() 实例方法，将组合参数解析器加到spring容器中，修改job()实例方法，加上**.validator(compositeValidator())。** 

右键启动，不填name参数，测试报错。如果放开name参数，传null值，一样报错。
