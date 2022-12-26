package oj.service;

import oj.pojo.Answer;
import oj.pojo.Question;

public interface TaskService {

    /**
     * 将完整的java代码进行编译运行的方法
     * @param question 用户提交的代码与测试用力代码拼接成最终运行的代码
     * @return 返回编译或运行的最终结果(错误码,错误原因,标准输出)
     */
    Answer compileAndRun(Question question);
}
