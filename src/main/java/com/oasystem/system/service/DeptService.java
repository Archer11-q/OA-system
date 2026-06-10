package com.oasystem.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.system.entity.Dept;

import java.util.List;

public interface DeptService extends IService<Dept> {
    List<Dept> listAll();

    Dept getByIdWithCheck(Long id);

    void createDept(Dept dept);

    void updateDept(Dept dept);

    void deleteDept(Long id);
}

