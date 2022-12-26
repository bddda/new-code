package oj.service;


import oj.exception.InsertException;
import oj.exception.PasswordNotMatchException;
import oj.exception.UsernameDuplicatedException;
import oj.exception.UsernameNotFoundException;
import oj.pojo.User;

public interface UserService {

    /**
     * 用户注册功能
     * @param user 用户数据
     */
    void register(User user) throws UsernameDuplicatedException, InsertException;

    /**
     * 用户登录功能
     * @param username
     * @param password
     * @return  当前匹配的用户数据没有则返回null值
     */
    User login(String username,String password) throws UsernameNotFoundException, PasswordNotMatchException;
}
