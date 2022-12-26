package oj;

import oj.mapper.UserMapper;
import oj.pojo.User;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserControllerTest {
    @Autowired
    private UserMapper userMapper;
    @Test
    void selectById(){
        User user = userMapper.selectById(1);
        System.out.println(user);
    }
    @Test
    void selectByName(){
        User user = userMapper.selectByName("zhw");
        System.out.println(user);
    }
    @Test
    void insert(){
        User user = new User();
        user.setUsername("admin1");
        user.setPassword("123123");
        user.setIsAdmin(0);
        user.setSalt("dsfsdfsfsfsfs");
        user.setEmail("wqdasfsfsf");
        userMapper.insert(user);
    }
    @Test
    void selectAll(){
        List<User> users = userMapper.selectAll();
        for (User user : users) {
            System.out.println(user);
        }
    }
    @Test
    void delete(){
        int deleteUser = userMapper.deleteUser(18);

        System.out.println(deleteUser);
    }
}
