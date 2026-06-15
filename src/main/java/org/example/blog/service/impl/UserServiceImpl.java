package org.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blog.entity.User;
import org.example.blog.mapper.UserMapper;
import org.example.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    //声明 UserMapper 字段,用于操作数据库(Spring 通过 @MapperScan 扫描)
//    private final UserMapper userMapper;

    //Spring 注入得到 UserMapper 对象,调用其方法对数据库进行操作;用 @Autowired 同理(只是写的不过于明确)
//    public UserServiceImpl(UserMapper userMapper){
//        this.userMapper = userMapper;
//    }

    @Override
    public void register(String email,String password,String nickname) {
        //LambdaQueryWrapper 和 QueryWrapper 作用相同--都是提供查询条件的构造器,即帮你写 SQL 的 where 语句;
        //不同点在于前者写错就会报错可以及时更改;而后者在运行时才报错,不容易排查
        //Wrapper 常用方法: eq->'=' ; ne->'!=' ; like->'%like' ; gt->'>' ; lt->'<' ; orderByDesc->降序排序 ; and/or
        //1.检查邮箱是否已被注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>(); //mybatis-plus 的查询条件构造器,用来组装 SQL 的 where 条件
        wrapper.eq(User::getEmail,email); //相当于 SQL where email = "输入的邮箱"
        if(this.count(wrapper)>0){ //统计符合条件的记录有几条->大于0抛出异常
            throw new RuntimeException("邮箱已被注册");
        }

        //解析这一块功能的代码以及涉及知识点
        //2.密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // BCrypt 哈希(每次加密结果不同)
        String encodePassword = passwordEncoder.encode(password);

        //3.创建用户并保存
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodePassword);
        user.setNickname(nickname);
        this.save(user);
    }

    @Override
    public User login(String email,String password) {
        //1.查看数据库有没有这个邮箱
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getEmail,email);
        User user = this.getOne(wrapper);
        //找到邮箱后获取
        if(user == null){
            throw new RuntimeException("邮箱未注册");
        }

        //2.检查密码是否匹配
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(!passwordEncoder.matches(password,user.getPassword())){
            throw new RuntimeException("密码错误");
        }

        return user;
    }
}
