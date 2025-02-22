package org.Roclh.data.telegramUser;

import org.Roclh.data.services.TelegramUserService;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.testutil.TelegramUserTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TelegramUserServiceTest extends TelegramUserTestBase {

    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private TelegramUserMocks tgMocks;

    @Test
    public void testUsersExists() {
        Assertions.assertAll(() -> Assertions.assertTrue(telegramUserService.exists(tgMocks.tgu1().getTelegramId())),
                () -> Assertions.assertTrue(telegramUserService.exists(tgMocks.tgu2().getTelegramId())),
                () -> Assertions.assertTrue(telegramUserService.exists(tgMocks.tgu3().getTelegramId())),
                () -> Assertions.assertTrue(telegramUserService.exists(tgMocks.tgu4().getTelegramId())),
                () -> Assertions.assertTrue(telegramUserService.exists(tgMocks.tgu5().getTelegramId())),
                () -> Assertions.assertTrue(telegramUserService.exists(tgMocks.tgu6().getTelegramId())),
                () -> Assertions.assertTrue(telegramUserService.exists(tgMocks.tguroot().getTelegramId()))
        );
    }
}
