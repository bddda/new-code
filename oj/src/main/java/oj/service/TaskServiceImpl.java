package oj.service;

import oj.pojo.Answer;
import oj.pojo.Question;
import oj.util.CommandUtil;
import oj.util.FileUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

//每次运行+编译作为一次Task
@Service
public class TaskServiceImpl implements TaskService{
    /*
    进程与进程之间互相独立,一个进程很难影响到其他进程,临时文件进行进程间通信
    */

    //所有临时文件所在的目录
    private static String WORK_DIR;

    //约定代码的类名
    private static final String CLASS="Solution";

    //约定要编译代码的文件名
    private static String CODE;

    //存放编译错误信息的文件名
    private static String COMPILE_ERROR;

    //存放运行错误信息的文件名
    private static String STDERR;

    //存放运行输出信息的文件名
    private static String STDOUT;

    public TaskServiceImpl() {
        WORK_DIR="./tmp/"+ UUID.randomUUID().toString()+"/";
        CODE=WORK_DIR+"Solution.java";
        COMPILE_ERROR = WORK_DIR+"compileError.txt";
        STDERR = WORK_DIR+"stderr.txt";
        STDOUT = WORK_DIR+"stdout.txt";
    }

    /**
     * 核心方法:编译+运行
     * @param question 用户提交的代码与测试用力代码拼接成最终运行的代码
     * @return 返回编译运行的结果,编译出错,运行出错,运行正确
     */
    @Override
    public Answer compileAndRun(Question question) {
        Answer answer = new Answer();
        File wordDir = new File(WORK_DIR);
        if (!wordDir.exists()){
            //如果路径不存在则创建多级路径
            wordDir.mkdirs();
        }

        //1.将源代码字符串code写入到Solution.java中去,类名和文件名一致,所以约定类名和文件名都叫做Solution
        FileUtil.writeFile(CODE,question.getCode()); //将code写到Solution.java中
        //2.调用javac编译,需要.java文件
        //如果编译出错,将错误信息写到COMPILE_ERROR,用一个文件保存compileError.txt,直接返回
        //此处需要先构造编译命令
        //utf8识别中文字符 -d 后面指定放置编译生成的.class文件的位置
        String compileCmd = String.format("javac -encoding utf8 %s -d %s", CODE, WORK_DIR);
        /*
        通过 Task 类与 " CommandUtil.run " 两者的结合，就能够让一个 ".java 文件 " 经过编译到运行完成的过程。
        而 Task 类最终返回的 Answer 对象，就是我们最终放到 HTTP 响应正文中的数据，它用作判定在线 OJ 代码的语法规范。
        */
        CommandUtil.run(compileCmd,null,COMPILE_ERROR);
        //如果编译出错,则这个错误信息就会被记录到COMPILE_ERROR文件中,如果没有出错则这个文件不存在
        String compileError = FileUtil.readFile(COMPILE_ERROR);
        if (compileError == null || !compileError.equals("")){
//            System.out.println("编译错误！");
            //编译出错直接返回answer
            answer.setError(1);
            //编译信息从编译错误的文件中读取
            answer.setReason(compileError);
            return answer;
        }
        //编译正确继续向下执行
        //3.再次创建子进程，调用java命令，执行.class文件，生成 stdout.txt stderr.txt
        //如果运行出错直接返回
        String runCmd = String.format("java -classpath %s %s", WORK_DIR, CLASS); //-classpath指定.class的位置
        CommandUtil.run(runCmd,STDOUT,STDERR);
        //读取运行时标准错误的文件,如果为空则返回answer,如果不为空则继续执行下面的逻辑
        String runStderr = FileUtil.readFile(STDERR);
        if (runStderr == null || !runStderr.equals("")){
//            System.out.println("运行错误！");
            //设置运行错误返回结果
            answer.setError(2);
            answer.setReason(runStderr);
            return answer;
        }
        //4.父进程获取到编译执行结果,打包成Answer对象返回
        answer.setError(0);
        answer.setStdout(FileUtil.readFile(STDOUT));
        return answer;
    }
}
