package oj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回值:表示一个task的执行结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Answer {
    //错误码,约定如果 error为0 表示编译运行都 ok error为1 表示编译出错 error为2 表示运行出错
    private int error;
    //错误信息,如果error为1编译出错了，reason就放编译的错误信息 如果error为2运行出错了，reason就放运行的错误信息
    private String reason;
    //运行程序得到的标准输出结果
    private String stdout;
    //运行程序得到的标准错误结果
    private String stderr;
}
