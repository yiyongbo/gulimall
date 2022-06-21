package com.yee.gulimall.thirdparty;

import com.yee.gulimall.thirdparty.component.SmsComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Autowired
    SmsComponent smsComponent;

    @Test
    void contextLoads() {
        // smsComponent.sendSmsCode("15079642472", "5873");
    }

}
