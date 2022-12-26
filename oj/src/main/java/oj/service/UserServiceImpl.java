package oj.service;

import oj.exception.InsertException;
import oj.exception.PasswordNotMatchException;
import oj.exception.UsernameDuplicatedException;
import oj.exception.UsernameNotFoundException;
import oj.mapper.UserMapper;
import oj.pojo.JsonResult;
import oj.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * 此类用于实现登陆注册等业务
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     * @param user 用户数据
     */
    @Override
    public void register(User user) throws UsernameDuplicatedException,InsertException{
        //通过user参数获取传递过来的name
        String username = user.getUsername();
        //判断用户是否已经存在
        User result = userMapper.selectByName(username);
        //判断结果集是否为null
        //如果不为null则说明用户已存在,则抛出用户被占用的异常
        if(result != null){
            throw new UsernameDuplicatedException("用户名已被注册!");
        }
        String email = user.getEmail();
        User byEmail = userMapper.selectByEmail(email);
        if (byEmail != null){
            throw new UsernameDuplicatedException("邮箱已被注册!");
        }
        /**
         * 乱码加密 MD5算法
         * 乱串 + password + 串 -------MD5算法进行加密,连续加载三次
         * 串成为盐值,一个字符串
         */
        String oldPassword = user.getPassword();
        //获取一个盐值(随机生成一个盐值)
        String salt = UUID.randomUUID().toString().toUpperCase();
        //获取加密之后的密码
        String md5PassWord = getMd5PassWord(oldPassword, salt);
        //补充密码相关信息
        user.setSalt(salt);//记录盐值,登陆的时候加密密码与数据库进行对比
        user.setPassword(md5PassWord);//这种方式可以忽略用户设置密码的强度
        //如果为null则继续注册
        Integer rows = userMapper.insert(user);
        //如果插入过程中服务器宕机则抛出异常
        if (rows != 1){
            throw new InsertException("在注册过程中产生了未知的异常");
        }
    }

    /**
     * 登录功能
     * @param username
     * @param password
     * @return
     */
    @Override
    public User login(String username, String password) throws UsernameNotFoundException,PasswordNotMatchException {
        //根据用户名来查询用户数据是否存在,如果不存在则抛出异常
        User user = userMapper.selectByName(username);
        if (user == null){
            throw new UsernameNotFoundException("该用户名不存在!");
        }
        //检测用户密码是否匹配
        //1.先获取数据库中加密的密码
        String oldPassword = user.getPassword();
        //2.和用户传递的密码经过三次md5加密之后的结果进行比较
        //2.1获取盐值
        String salt = user.getSalt();
        //2.2调用方法进行加密
        String md5PassWord = getMd5PassWord(password, salt);
        //比较如果用户输入的密码经过三次加密后的结果和从数据库中拿到的密码一样则返回user,否则抛出异常
        if(!oldPassword.equals(md5PassWord)){
            throw new PasswordNotMatchException("用户密码输入错误!");
        }
        //此时密码正确
        return user;
    }




    /**
     * 对密码进行三次加密
     * @param password 用户传递的原密码
     * @param salt  随机生成的盐值
     * @return  返回的新密码
     */
    public static String getMd5PassWord(String password,String salt){
        // springBoot提供了一个加密工具类 DigestUtils

        for(int i = 0;i<3;i++){
            password = DigestUtils.md5DigestAsHex((salt+password+salt).getBytes()).toUpperCase();
        }

        // 返回加密之后的密码
        return password;
    }
}
