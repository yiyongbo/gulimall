package com.yee.gulimall.thirdparty.component;

import com.yee.gulimall.thirdparty.util.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YYB
 */
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
@Component
public class SmsComponent {

    private String host;
    private String path;
    private String appCode;

    public void sendSmsCode(String phone, String code) {

        String method = "POST";
        Map<String, String> headers = new HashMap<>(16);
        // 最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appCode);
        Map<String, String> querys = new HashMap<>(16);
        // 测试请使用默认模板：【创信】你的验证码是：5873，3分钟内有效！；
        querys.put("content", "【创信】你的验证码是：" + code + "，3分钟内有效！");
        querys.put("mobile", phone);
        Map<String, String> bodys = new HashMap<>(16);


        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
