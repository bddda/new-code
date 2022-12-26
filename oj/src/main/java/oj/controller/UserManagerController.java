package oj.controller;

import oj.exception.DeleteException;
import oj.exception.UpdateException;
import oj.mapper.UserMapper;
import oj.pojo.JsonResult;
import oj.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/userManager")
public class UserManagerController extends BaseController{
    @Autowired
    public UserMapper userMapper;

    /**
     * 根据邮箱确认用户是否存在
     * @param email
     * @return
     */
    @RequestMapping(value = "/emailUser",produces = "application/json;charset=utf8")
    public JsonResult<User> retUser(String email){
        JsonResult<User> result = new JsonResult<>();
        User user = userMapper.selectByEmail(email);
        if (user == null){
            //输入的邮箱虽然存在但是与用户不绑定,也就是说这个邮箱不是存在数据库中的信息
            result.setState(6080);
            result.setMessage("该邮箱未注册,响应失败!");
            return result;
        }
        result.setState(6081);
        result.setMessage("匹配成功,该用户存在");
        result.setData(user);
        return result;
    }

    /**
     * 判断是否是管理员用户
     */
    @RequestMapping("/isLoad")
    public JsonResult<Void> load(HttpSession session){
        JsonResult<Void> result = new JsonResult<>();
        User user = (User) session.getAttribute("user");
        if (user.getIsAdmin() == 1){
            result.setState(6050);
            result.setMessage("当前用户是管理员用户!");
            return result;
        }
        result.setState(6051);
        result.setMessage("当前用户只是普通用户!");
        return result;
    }
    /**
     * 查询所有用户
     */
    @RequestMapping("/selectAll")
    public JsonResult<List<User>> selectAll(){
        JsonResult<List<User>> result = new JsonResult<>();
        List<User> userList = userMapper.selectAll();
        if (userList.isEmpty()){
            result.setState(6020);
            result.setMessage("服务器异常,查询题目列表失败!");
            return result;
        }
        result.setState(6021);
        result.setMessage("查询成功!");
        return result;
    }

    /**
     * 模糊查询,根据名字
     */
    @RequestMapping("/selectByLikeName")
    //@RequestParam(required = false)前端不传参数的时候,会将参数置为null
    public JsonResult<List<User>> getUser(@RequestParam(required = false) String username){
        JsonResult<List<User>> result = new JsonResult<>();
        if (username == null){
            result.setState(6040);
            result.setMessage("参数非法!");
            return result;
        }
        List<User> byLikeName = userMapper.selectByLikeName("%" + username.trim() + "%");
        if (byLikeName == null || byLikeName.isEmpty()){
            result.setState(6041);
            result.setMessage("此次查询未查找到相关结果!");
            return result;
        }
        result.setState(6042);
        result.setMessage("查找结果已显示在当前页面中!");
        result.setData(byLikeName);
        return result;
    }
    /**
     * 根据id查询
     */
    @RequestMapping("/selectById")
    public JsonResult<User> selectOne(String id){
        JsonResult<User> result = new JsonResult<>();
        User user = userMapper.selectById(Integer.parseInt(id));
        result.setMessage("查询成功!");
        result.setData(user);
        return result;
    }
    /**
     * 修改用户信息
     */
    @RequestMapping("/updateUser")
    public JsonResult<Void> updateUser(String id,@RequestBody User user) throws UpdateException {
        JsonResult<Void> result = new JsonResult<>();
        int rows = userMapper.updateUser(Integer.parseInt(id), user.getUsername(), user.getEmail());
        if (rows != 1){
            throw new UpdateException("服务器异常修改失败!");
        }
        result.setState(6070);
        result.setMessage("修改成功");
        return result;
    }
    /**
     * 删除用户信息
     */
    @RequestMapping("/deleteUser")
    public void deleteUser(String id, HttpServletResponse response) throws DeleteException, IOException {
        int idString = Integer.parseInt(id);
        int deleteUser = userMapper.deleteUser(idString);
        if(deleteUser!=1){
            //System.out.println("删除失败!");
            throw new DeleteException("删除失败!");
        }
        response.sendRedirect("../userManage.html");
    }
}
