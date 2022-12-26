package oj;

import oj.pojo.Answer;
import oj.pojo.Question;
import oj.service.TaskServiceImpl;
import org.junit.Test;

public class TaskTest {

    @Test
    public void test(){
        TaskServiceImpl task = new TaskServiceImpl();
        Question question = new Question();
        question.setCode("public class Solution {\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"hello world!\");\n" +
                "    }\n" +
                "}");
        Answer answer = task.compileAndRun(question);
        System.out.println(answer);

    }
}
