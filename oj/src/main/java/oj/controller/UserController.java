package oj.controller;

import oj.exception.*;
import oj.mapper.UserMapper;
import oj.pojo.JsonResult;
import oj.pojo.User;
import oj.service.SendSmsService;
import oj.service.UserService;
import oj.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 此类处理专门处理用户的各种状态
 * 登录，注册，注销
 */

@RestController
@RequestMapping("/state")
public class UserController extends BaseController{
    @Autowired
    private UserService userService;

    /**
     * RedisTemplate中定义了对5种数据结构操作
     * redisTemplate.opsForValue();//操作字符串
     * redisTemplate.opsForHash();//操作hash
     * redisTemplate.opsForList();//操作list
     * redisTemplate.opsForSet();//操作set
     * redisTemplate.opsForZSet();//操作有序set
     */
    @Resource
    public RedisTemplate<String,Object> redisTemplate;

    @Autowired
    public JavaMailSender javaMailSender;  //发送邮件

    @Autowired
    public SendSmsService sendSmsService;  //发送手机验证码

    @Autowired
    public UserMapper userMapper;

    @Value("${spring.mail.from}")
    public String mailFrom;

    public Random random = new Random();

    /**
     *
     * 请求的路径 users/reg
     * 这是接收注册的请求 User user
     * 响应结果 JsonResult<void>
     */

    /**
     * 注册的逻辑
     * 1.如果用户名和密码都确认为空,返回提示信息:用户名和密码不能为空
     * 2.此时都不为空,如果确认密码和密码不一致,则返回提示信息:两次密码不一致,请重新输入
     * 3.此时都不为空且两次密码都一致,查询数据库,如果用户名已存在,则抛出异常返回提示信息:用户名已被占用
     * 4.此时都没有问题,但是在执行数据库插入操作的时候服务器断开连接或者宕机了,则抛出异常返回提示信息:服务器连接异常,注册失败!
     * 5.注册成功前端跳转到登录页面
     */
    static class RegUser{
        public String username;
        public String password;
        public String email;
        public String emailCode;
    }
    @RequestMapping(value = "/reg",produces = "application/json;charset=utf8")
    public JsonResult<Void> register(@RequestBody RegUser regUser) throws InsertException,UsernameDuplicatedException {
        JsonResult<Void> result = new JsonResult<>();
        User user = new User();
        //如果邮箱地址存在,则验证码必然存在,前端决定,反之则必然不存在
        if (!regUser.email.equals("")){
            //说明邮箱地址存在,对比验证码
            if (redisTemplate.opsForValue().get(regUser.email) == null){
                //如果redis中查不到的话说明这个邮箱地址写了但是没法验证码,没有保存到redis中
                result.setMessage("验证码输入错误，请重新输入!");
                return result;
            }
            String redisCode = redisTemplate.opsForValue().get(regUser.email).toString();
            if (!redisCode.equals(regUser.emailCode)){
                //对比失败
                return result;
            }else {
                user.setEmail(regUser.email);
            }
        }
        user.setUsername(regUser.username);
        user.setPassword(regUser.password);

        userService.register(user);
        String userEmail = user.getEmail();
        result.setState(1003);
        result.setMessage("注册成功!");
        return result;
    }

    /**
     * 用户名密码登录逻辑
     * 1.对传入的参数进行非空校验,如果有空的则返回提示信息:用户名或密码不能为空
     * 2.如果都不为空,根据用户名进行查找,如果不存在则用户名存在异常,返回提示信息:用户名不存在,请重新输入
     * 3.如果用户名存在,则将用户名和输入的密码(包含盐值)进行匹配,如果不配不上则返回提示信息:密码错误,请重新输入
     * 4.如果密码输入正确则返回登陆成功提示信息,前端跳转到主页
     */
    @RequestMapping(value = "/login",produces = "application/json;charset=utf8")
    public JsonResult<Void> loginByUser(String username, String password, HttpServletRequest request) throws UsernameNotFoundException, PasswordNotMatchException {
        JsonResult<Void> result = new JsonResult<>();
        //对参数进行非空校验
        if(username == null || "".equals(username) || password == null || "".equals(password)){
            result.setState(3003);
            result.setMessage("用户名和密码不能为空!");
            return result;
        }
        //如果都不为空,则执行登陆业务
        User user = userService.login(username, password);
        result.setState(3004);
        result.setMessage("登录成功!");

        //设置全局session
        HttpSession session = request.getSession(true);
        session.setAttribute("user",user);
        return result;
    }

    /**
     * 发送邮件信息
     * SimpleMailMessage类中:
     *      message.setFrom(from); 设置发件人Email
     *      message.setSubject(subject); 设置邮件主题
     *      message.setText(content);   设置邮件主题内容
     *      message.setTo(to);          设定收件人Email
     */
    @RequestMapping("/emailSend")
    public JsonResult<Void> sendEmail(String To){
        //对邮件格式进行校验(前端完成)
        JsonResult<Void> result = new JsonResult<>();
        if (!isEmail(To)){
            result.setState(5003);
            result.setMessage("输入的邮箱格式非法，请重新输入!");
            return result;
        }
        if (!StringUtils.isEmpty(redisTemplate.opsForValue().get(To))){
            // 如果在redis中查到已经发送过还没有过期，那么就先不发
            result.setState(5004);
            result.setMessage("验证码未过期，无法发送，请及时查看");
            return result;
        }
        //邮件发送功能
        SimpleMailMessage message = new SimpleMailMessage();
        String newCode = getNewCode();
        message.setSubject("注册");
        message.setText("您注册的验证码是:" + newCode + ",有效期两分钟请及时填写!");
        message.setFrom(mailFrom);
        message.setTo(To);
        javaMailSender.send(message);
        //==============将验证码保存redis中设置 过期时间 120s==============
        redisTemplate.opsForValue().set(To,newCode,2, TimeUnit.MINUTES);
        result.setState(5005);
        result.setMessage("验证码发送成功，2分钟内有效，请及时填写!");
        return result;
    }

    /**
     * 手机登录逻辑
     * 验证参数phone和 code是否为空,如果为空则抛出异常返回提示信息:手机号和验证码不能为空
     * 如果不为空,则根据手机号在redis中查询验证码
     * 如果验证码不存在或者过期,则抛出异常返回提示信息:手机号输入错位或超出有效输入时间,登陆失败!
     * 如果验证码存在,则进行对比,如果对比成功,则登录成功,前端跳转到主页;对比失败则登录失败返回提示信息:验证码输入错误,登陆失败!
     */
    @RequestMapping("/phoneLogin")
    public JsonResult<Void> phoneLog(String phone,String code,HttpSession session){
        JsonResult<Void> result = new JsonResult<>();
        if (phone == null || "".equals(phone.trim()) || code == null || "".equals(code.trim())){
            result.setState(4005);
            result.setMessage("手机号或验证码不能为空,请重新输入");
            return result;
        }
        if (phone.trim().length() != 11 || code.trim().length() != 6){
            result.setState(4006);
            result.setMessage("手机号或验证码格式非法,请重新输入");
            return result;
        }
        String phoneCode = (String) redisTemplate.opsForValue().get(phone.trim());
        if (phoneCode == null){
            //手机号输入错位或超出有效输入时间,登陆失败!
            result.setState(4007);
            result.setMessage("手机号输入错位或超出有效输入时间,登陆失败!");
            return result;
        }
        //如果验证码存在,进行对比
        if (!code.equals(phoneCode.trim())){
            result.setState(4008);
            result.setMessage("验证码输入错误,登陆失败");
            return result;
        }
        //验证码对比成功,设置全局session返回登录成功
        session.setAttribute("phone",phone.trim());
        result.setState(4009);
        result.setMessage("手机号登录成功!");
        return result;
    }

    /**
     * 发送手机验证码的逻辑
     * 1.先验证手机号是否非法,非空,手机号11位
     * 2.连接redis,查找手机验证码是否仍然未过期,如果未过期的话那么不能发送,返回信息:验证码未逾期,再次发送失败
     * 3.查询数据库如果不存在验证码的话,则生成6位随机数,发送验证码
     *      如果发送失败，那么返回结果提示 验证码发送失败，为什么会发送失败？腾讯云每天对发送短信条数频率是会限制的，发送短信是要钱的，个人云账户没钱了等情况都会发生发送失败！
     *      如果发送成功,则保存到redis中设置时间为2分钟,返回提示信息:验证码发送成功,2分钟有效,请及时填写
     */
    @RequestMapping(value = "/phoneSend",produces = "application/json;charset-utf8")
    public JsonResult<Void> sendCode(@RequestParam(value = "phone") String phone){
        JsonResult<Void> result = new JsonResult<>();
        //验证空参
        if (phone == null || "".equals(phone)){
            result.setState(4000);
            result.setMessage("手机号不能为空,请重新输入!");
            return result;
        }
        //验证非法
        if (phone.length() != 11){
            result.setState(4001);
            result.setMessage("手机号格式非法,请重新输入!");
            return result;
        }
        //1.连接redis查找手机验证码是否存在
        String code = (String) redisTemplate.opsForValue().get(phone);
        //1.1如果存在说明已经发送过验证码,不能重新发送了
        if (!StringUtils.isEmpty(code)){
            result.setState(4002);
            result.setMessage("验证码还未过期，再次发送失败!");
            return result;
        }
        //1.2如果不存在,那么redis创建键值对验证码并储存,设置超时时间
        String newCode = getNewCode();
        //将6位验证码对手机号进行发送
        boolean isSend = sendSmsService.send(phone, "1511343", newCode);
        // 因为有短信轰炸的情况，短信服务对每次发送限制次数，所以有发送不成功的情况，要考虑
        //如果发送成功,则将验证码存到redis中
        if (isSend){
            redisTemplate.opsForValue().set(phone,newCode,2,TimeUnit.MINUTES);
            result.setState(4003);
            result.setMessage("验证码发送成功,2分钟内有效,请及时填写!");
        }else {
            result.setState(4004);
            result.setMessage("验证码发送失败!");
        }
        return result;
    }

    @RequestMapping("/loginOut")
    public JsonResult<Void> loginOut(HttpSession session){
        JsonResult<Void> result = new JsonResult<>();
        if (session == null || session.getAttribute("phone") == null && session.getAttribute("user") == null){
            result.setState(5001);
            result.setMessage("注销失败");
            return result;
        }
        if (session.getAttribute("phone") != null){
            session.removeAttribute("phone");
        }
        if (session.getAttribute("user") != null){
            session.removeAttribute("user");
        }
        //注销成功
        result.setState(5002);
        result.setMessage("注销成功!");
        return result;
    }

    static class Require{
        public String email;
        public String code;
    }
    @RequestMapping("/emailRequire")
    public JsonResult<Void> requireEmail(@RequestBody Require require){
        JsonResult<Void> result = new JsonResult<>();
        String email = require.email;
        String code = require.code;
        //查询redis中是否存在email
        String OldEmail = (String) redisTemplate.opsForValue().get(email);
        //未查到的话则返回信息提示:邮箱输入错误或验证码已过期!
        if (OldEmail == null){
            result.setState(5006);
            result.setMessage("邮箱输入错误或验证码已过期!");
            return result;
        }
        //若查到则用查到的验证码与用户输入的验证码进行对比
        if (!code.equals(OldEmail)){
            //匹配失败则返回提示信息:输入的验证码错误，请重新输入!
            result.setState(5007);
            result.setMessage("输入的验证码错误，请重新输入!");
            return result;
        }
        //匹配成功则返回提示信息:验证成功并跳转到重置密码的页面
        result.setState(5008);
        result.setMessage("验证成功");
        return result;
    }

    static class UpdatePassword{
        public String username;
        public String password;
    }

    @RequestMapping("/updatePassword")
    public JsonResult<Void> updatePassword(@RequestBody UpdatePassword updatePassword) throws UpdateException {
        JsonResult<Void> result = new JsonResult<>();
        //输入密码的逻辑
        String username = updatePassword.username;
        String password = updatePassword.password;
        //获取原先用户的盐值,将密码进行MD5加密
        User user = userMapper.selectByName(username);
        String salt = user.getSalt();
        //获取用户修改过后的密码经过加密后的密码,替换即可
        String finalPassword = UserServiceImpl.getMd5PassWord(password, salt);
        //最后将原密码换位MD5加密后的密码
        int rows = userMapper.updatePassword(username, finalPassword);
        if (rows != 1){
            throw new UpdateException("服务器异常，插入失败!");
        }
        result.setMessage("修改成功");
        return result;
    }

    /**
     * 生成6位随机码的方法
     * @return
     */
    private String getNewCode(){
        String newCode = "";
        for (int i = 0;i < 6;i++){
            newCode += random.nextInt(10);
        }
        return newCode;
    }

    /**
     * 校验邮箱格式
     */
    public static boolean isEmail(String str){
        String tegex="[a-zA-Z0-9_]+@\\w+(\\.com|\\.cn){1}";
        //matches检测字符串是否匹配给定的正则表达式
        boolean matches = str.matches(tegex);
        return matches;
    }
}
