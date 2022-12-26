package oj.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config.properties") //加载资源文件
@ComponentScan(basePackages = "oj.pojo") //扫描组件,将属性替换。
                                        // 这个注解会让spring去扫描某些包及其子包中所有的类，
                                        //然后将满足一定条件的类作为bean注册到spring容器容器中。
public class JavaConfig {
}
