package oj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库表的实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    //题目的id，自增主键
    private int id;
    //题目的标题
    private String title;
    //题目的难度
    private String level;
    //题目描述
    private String description;
    //编程的初始模板
    private String template;
    //编程题目的测试用例
    private String testCode;
}
