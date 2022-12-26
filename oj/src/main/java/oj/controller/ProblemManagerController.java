package oj.controller;

import oj.exception.DeleteException;
import oj.exception.InsertException;
import oj.exception.UpdateException;
import oj.mapper.ProblemMapper;
import oj.pojo.JsonResult;
import oj.pojo.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/problemManage")
public class ProblemManagerController extends BaseController{
    @Autowired
    public ProblemMapper problemMapper;

    /**
     * 增加题目的接口
     * @param problem
     * @return 7000-7009
     */
    @RequestMapping("/insert")
    public JsonResult<Void> add(@RequestBody Problem problem) throws InsertException {
        JsonResult<Void> result = new JsonResult<>();
        if (problem == null){
            result.setMessage("参数非法");
            return result;
        }
        if (problem.getTitle().trim().equals("")||
            problem.getLevel().trim().equals("")||
            problem.getDescription().trim().equals("")||
            problem.getTemplate().trim().equals("")||
            problem.getTestCode().trim().equals("")
        ){
            result.setMessage("您提交的信息不完整，请补全题目信息在提交!");
            return result;
        }
        int insert = problemMapper.insert(problem);
        if (insert != 1){
            throw new InsertException("添加失败!");
        }
        result.setMessage("添加成功!");
        return result;
    }

    /**
     * 修改题目的接口,按照题目id进行修改
     */
    @RequestMapping("/update")
    public JsonResult<Void> update(String id,@RequestBody Problem problem) throws UpdateException {
        JsonResult<Void> result = new JsonResult<>();
        if (id == null || "".equals(id) || problem == null){
            result.setMessage("参数非法!");
            return result;
        }
        if (problem.getTitle().trim().equals("")||
                problem.getLevel().trim().equals("")||
                problem.getDescription().trim().equals("")||
                problem.getTemplate().trim().equals("")||
                problem.getTestCode().trim().equals("")
        ){
            result.setMessage("填入的的信息不完整，请补全题目信息在提交!");
            return result;
        }
        int idString = Integer.parseInt(id);
        int update = problemMapper.updateById(idString, problem.getTitle().trim(), problem.getLevel().trim(),
                problem.getDescription().trim(), problem.getTemplate().trim(), problem.getTestCode().trim());
        if (update != 1){
            throw new UpdateException("修改失败!");
        }
        result.setMessage("修改成功!");
        return  result;
    }

    /**
     * 删除题目的接口,按照id删除
     */
    @RequestMapping("/delete")
    public JsonResult<Void> delete(String id,HttpServletResponse response) throws DeleteException, IOException {
        JsonResult<Void> result = new JsonResult<>();
        if (id == null || "".equals(id)){
            result.setMessage("id不能为空");
            return result;
        }
        int parseInt = Integer.parseInt(id);
        int delete = problemMapper.delete(parseInt);
        if (delete != 1){
            throw new DeleteException("删除失败!");
        }
        result.setMessage("修改成功!");
        response.sendRedirect("../problemManage.html");
        return result;
    }

    /**
     * 根据名字进行模糊查询
     */
    @RequestMapping("/selectLike")
    public JsonResult<List<Problem>> select(String title){
        JsonResult<List<Problem>> result = new JsonResult<>();
        if (title == null){
            result.setState(7031);
            result.setMessage("非法参数");
            return result;
        }
        List<Problem> list = problemMapper.selectByLikeTitle(title.trim());
        if (list == null || list.isEmpty()){
            result.setState(7032);
            result.setMessage("未查询到相关题目信息!");
            return result;
        }
        result.setState(7033);
        result.setMessage("查询到相关信息!");
        result.setData(list);
        return result;
    }

    /**
     * 查询所有题目的接口
     */
    @RequestMapping("/selectAll")
    public JsonResult<List<Problem>> selectAll(){
        JsonResult<List<Problem>> result = new JsonResult<>();
        List<Problem> selectAll = problemMapper.selectAll();
        if (selectAll == null || selectAll.isEmpty()){
            result.setMessage("未查询到相关题目信息!");
            return result;
        }
        result.setMessage("查询到相关信息!");
        result.setData(selectAll);
        return result;
    }

    /**
     * 根据id查询
     */
    @RequestMapping("/selectById")
    public JsonResult<Problem> selectById(String id){
        JsonResult<Problem> result = new JsonResult<>();
        if (id == null || "".equals(id)){
            result.setMessage("id参数不合法!");
            return result;
        }
        int idString = Integer.parseInt(id);
        Problem problem = problemMapper.selectOne(idString);
        if (problem == null){
            result.setMessage("未查询到相关题目信息!");
            return result;
        }
        result.setState(7051);
        result.setMessage("已查询到相关信息显示在当前界面上");
        result.setData(problem);
        return result;
    }
}
