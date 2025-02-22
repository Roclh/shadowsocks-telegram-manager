package org.Roclh.testutil;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.TestBase;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.mock.TelegramUserMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class TelegramUserTestBase extends TestBase {

    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private TelegramUserMocks tgMocks;

    @BeforeEach
    public void init() {
        log.info("Initiating telegram users in database");
        telegramUserService.saveUser(tgMocks.tguroot());
        telegramUserService.saveUser(tgMocks.tgu1());
        telegramUserService.saveUser(tgMocks.tgu2());
        telegramUserService.saveUser(tgMocks.tgu3());
        telegramUserService.saveUser(tgMocks.tgu4());
        telegramUserService.saveUser(tgMocks.tgu5());
        telegramUserService.saveUser(tgMocks.tgu6());
    }


    @AfterEach
    public void destroy() {
        log.info("Destroying telegram users in database");
        telegramUserService.clear();
    }

}
