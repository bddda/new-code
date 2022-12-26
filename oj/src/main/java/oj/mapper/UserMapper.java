package oj.mapper;

import oj.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> selectAll();

    //用户名模糊查询
    List<User> selectByLikeName(String likeName);

    User selectByNameAndId(String id,String name);

    int updateUser(@Param("id") int id,@Param("username") String username,@Param("email") String email);

    int deleteUser(@Param("id") int id);

    User selectById(int id);

    User selectByName(String name);

    User selectByEmail(String email);

    int insert(User user);

    int updatePassword(@Param("username") String username, @Param("finalPassword") String finalPassword);
}
