package oj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示一个Task要输入的内容
 */

@Data
@AllArgsConstructor  //将被其标注的类里边的全部变量都生成全参数的构造器
@NoArgsConstructor   //生成没有参数的构造器
public class Question {
    //包含要编译的代码
    private String code;
}
