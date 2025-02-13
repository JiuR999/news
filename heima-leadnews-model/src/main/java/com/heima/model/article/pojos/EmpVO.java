package com.heima.model.article.pojos;

import lombok.Data;

import java.util.List;

@Data
public class EmpVO extends TestDep{
    List<TestEmp> emps;
}
