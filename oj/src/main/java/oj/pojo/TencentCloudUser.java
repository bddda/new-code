package oj.pojo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TencentCloudUser implements InitializingBean {
    @Value("${secretId}")
    private String secretId;
    @Value("${secretKey}")
    private String secretKey;

    public static String ID;
    public static String KEY;


    @Override
    //afterPropertiesSet 初始化bean的时候执行，可以针对某个具体的bean进行配置。
    //afterPropertiesSet 必须实现 InitializingBean接口。
    //实现 InitializingBean接口必须实现afterPropertiesSet方法。
    public void afterPropertiesSet() throws Exception {
        ID=this.secretId;
        KEY=this.secretKey;
    }
}
