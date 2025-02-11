package org.Roclh.test;

import org.Roclh.TestBase;
import org.Roclh.bot.TelegramBotStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public class TelegramStartupTest extends TestBase {

    @Autowired
    private TelegramBotStorage telegramBotStorage;

    @Test
    void testTelegramBotCreatedAndAvailableFromBotStorage(){
        Assert.notNull(telegramBotStorage.getTelegramBot(), "Telegram bot storage is empty!");
    }

}
