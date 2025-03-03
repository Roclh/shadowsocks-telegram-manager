package org.Roclh.mock;

import org.Roclh.data.enums.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.springframework.stereotype.Component;

@Component
public class TelegramUserMocks {

    public TelegramUserModel tguroot(){
        return TelegramUserModel.builder()
                .chatId(1000L)
                .telegramId(1000L)
                .role(Role.ROOT)
                .telegramName("TestRoot")
                .build();
    }

    public TelegramUserModel tgu1(){
        return TelegramUserModel.builder()
                .chatId(1L)
                .telegramId(1L)
                .role(Role.USER)
                .telegramName("Test1")
                .build();
    }

    public TelegramUserModel tgu2(){
        return TelegramUserModel.builder()
                .chatId(2L)
                .telegramId(2L)
                .role(Role.GUEST)
                .telegramName("Test2")
                .build();
    }

    public TelegramUserModel tgu3(){
        return TelegramUserModel.builder()
                .chatId(3L)
                .telegramId(3L)
                .role(Role.GUEST)
                .telegramName("Test3")
                .build();
    }

    public TelegramUserModel tgu4(){
        return TelegramUserModel.builder()
                .chatId(4L)
                .telegramId(4L)
                .role(Role.MANAGER)
                .telegramName("Test4")
                .build();
    }

    public TelegramUserModel tgu5(){
        return TelegramUserModel.builder()
                .chatId(5L)
                .telegramId(5L)
                .role(Role.USER)
                .telegramName("Test5")
                .build();
    }

    public TelegramUserModel tgu6(){
        return TelegramUserModel.builder()
                .chatId(6L)
                .telegramId(6L)
                .role(Role.GUEST)
                .telegramName("Test6")
                .build();
    }

    public TelegramUserModel tgNewU(){
        return TelegramUserModel.builder()
                .chatId(7L)
                .telegramId(7L)
                .role(Role.GUEST)
                .telegramName("TestNew7")
                .build();
    }
}
