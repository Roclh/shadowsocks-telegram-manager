package org.Roclh.base;

import org.Roclh.TestBase;
import org.Roclh.bot.TelegramBotStorage;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.mock.ShScriptsMocks;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

@Import(ShScriptsMocks.class)
public class TelegramStartupTest extends TestBase {

    @Autowired
    private TelegramBotStorage telegramBotStorage;

    @Autowired
    private TelegramUserService telegramUserService;

    @Test
    void testTelegramBotCreatedAndAvailableFromBotStorage(){
        Assert.notNull(telegramBotStorage.getTelegramBot(), "Telegram bot storage is empty!");
        Assert.notEmpty(telegramUserService.getUsers(), "At least one user should exist");
        Assert.isTrue(!telegramUserService.getUsers(user -> user.getRole().prior >= 3).isEmpty(),
                "At least one user should be root");
    }

}
