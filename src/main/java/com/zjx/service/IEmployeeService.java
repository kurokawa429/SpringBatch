package com.zjx.service;

import com.zjx.entity.Employee;

import java.io.IOException;

public interface IEmployeeService {
    /**
     * 保存
     */
    void save(Employee employee);

    void dataInit() throws IOException;


    /**
     * 清空数据
     */
    void truncateAll();

    /**
     * 清空employee_temp数据
     */
    void truncateTemp();
}

