package oj.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor  //将被其标注的类里边的全部变量都生成全参数的构造器
@NoArgsConstructor   //生成没有参数的构造器
public class User {
    //用户id
    private int id;
    //用户名
    private String username;
    //用户密码
    private String password;
    //是否为管理员 0为普通用户,1为管理员
    private int isAdmin;
    //盐值
    private String salt;
    //邮箱用于找回个人密码
    private String email;
}
