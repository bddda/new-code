package oj.controller;

import oj.exception.ProblemNotFountException;
import oj.mapper.ProblemMapper;
import oj.pojo.Answer;
import oj.pojo.JsonRequest;
import oj.pojo.Problem;
import oj.pojo.Question;
import oj.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/oj")
@CrossOrigin //为整个controller启用跨域支持
public class CompileController extends BaseController{
    @Autowired
    public ProblemMapper problemMapper;

    @Autowired
    public TaskService taskService;

    @PostMapping(value = "/compile",produces = "application/json")
    public Answer getCompile(@RequestBody JsonRequest jsonRequest) throws ProblemNotFountException {

        //0.读取传递的正文和id,json传递
        int id = Integer.parseInt(jsonRequest.getId());
        String code = jsonRequest.getCode();
        //0.5检查用户提交代码的安全性,通过对list的黑名单进行查找
        if (!checkCodeSafe(code)){
            Answer answer = new Answer();
            answer.setError(3);
            answer.setReason("您提交了不安全的代码，危害服务器安全，编译失败!");
            return answer;
        }
        //1.从数据库中找到题目拿到测试用例的代码
        Problem problem = problemMapper.selectOne(id);
        if (problem == null){
            throw new ProblemNotFountException("提交的id非法，找不到题目信息!");
        }
        String testCode = problem.getTestCode();
        //2.将用户提交的代码和测试用例的代码拼接成一个完整的代码
        String finalCode = mergeCode(code,testCode);
        //3.创建一个task,调用编译运行结果
        Question question = new Question();
        question.setCode(finalCode);
        Answer answer = taskService.compileAndRun(question);
        return answer;
    }

    private String mergeCode(String code, String testCode) {
        //查找code中最后一个 }
        int pos = code.lastIndexOf("}");
        if (pos == -1){
            //没找到的话直接返回null,无法拼接
            return null;
        }
        //code截取之后与testCode拼接
        return code.substring(0,pos)+testCode+"\n}";
    }

    //检查代码安全性的方法
    private boolean checkCodeSafe(String code) {
        List<String> black = new ArrayList<>();
        //黑名单随时扩充,防止提交的代码危害服务安全
        black.add("Runtime");// 防止提交的代码运行恶意程序
        black.add("exec");
        black.add("java.io");// 禁止提交的代码读写文件
        black.add("java.net"); // 禁止提交的代码访问网络
        for (String s : black) {
            //返回某个指定的字符串值在字符串中首次出现的位置
            int pos = code.indexOf(s);
            if (pos >= 0){
                //找到任意的恶心代码特征,返回false表示不安全
                return false;
            }
        }
        return true;
    }
}
