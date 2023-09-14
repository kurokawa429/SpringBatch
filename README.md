# SpringBatch
1>先动态生成50w条员工数据，存放在employee.csv文件中

2>启动作业异步读取employee.csv文件，将读到数据写入到employee_temp表，要求记录读与写消耗时间

3>使用分区的方式将employee_temp表的数据读取并写入到employee表
