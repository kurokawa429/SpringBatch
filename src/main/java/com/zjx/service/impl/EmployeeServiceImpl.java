package com.zjx.service.impl;

import com.zjx.entity.Employee;
import com.zjx.mapper.EmployeeMapper;
import com.zjx.service.IEmployeeService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

@Service
public class EmployeeServiceImpl implements IEmployeeService {

    @Value("${job.data.path}")
    public String path;

    @Setter(onMethod_ = {@Autowired})
    private EmployeeMapper employeeMapper;

    @Override
    public void save(Employee employee) {
        employeeMapper.save(employee);
    }

    @Override
    public void dataInit() throws IOException {
        File file = new File(path, "employee.csv");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        String txt = "";
        Random ageR = new Random();
        Random boolR = new Random();
        try (FileOutputStream out = new FileOutputStream(file)) {
            // 给文件中生产50万条数据
            long beginTime = System.currentTimeMillis();
            System.out.println("开始时间：【 " + beginTime + " 】");
            for (int i = 1; i <= 500000; i++) {
                if(i == 500000){
                    txt = i+",zjx_"+ i +"," + ageR.nextInt(100) + "," + (boolR.nextBoolean()?1:0);
                }else{
                    txt = i+",zjx_"+ i +"," + ageR.nextInt(100) + "," + (boolR.nextBoolean()?1:0) +"\n";
                }

                out.write(txt.getBytes());
            }
            System.out.println("总共耗时：【 " + (System.currentTimeMillis() - beginTime) + " 】毫秒");
        }

        }

    @Override
    public void truncateAll() {
        employeeMapper.truncateAll();
    }

    @Override
    public void truncateTemp() {
        employeeMapper.truncateTemp();
    }


}
