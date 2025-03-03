package org.Roclh.mock;

import org.Roclh.data.entities.UserModel;
import org.Roclh.data.enums.Plugin;
import org.Roclh.data.services.TelegramUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMocks {
    @Autowired
    private TelegramUserService tgUserService;

    public UserModel u1(){
        return UserModel.builder()
                .usedPort(8000L)
                .plugin(Plugin.DEFAULT)
                .isEnabled(true)
                .userModel(tgUserService.getUser(1L).orElseThrow())
                .password("TestPassword1")
                .build();
    }

    public UserModel u2(){
        return UserModel.builder()
                .usedPort(8001L)
                .plugin(Plugin.V2RAY)
                .isEnabled(false)
                .userModel(tgUserService.getUser(4L).orElseThrow())
                .password("TestPassword2")
                .build();
    }
}
