package org.Roclh.testutil;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.UserService;
import org.Roclh.mock.UserMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class UserTestBase extends TelegramUserTestBase {

    @Autowired
    private UserMocks userMocks;
    @Autowired
    private UserService userService;

    @Override
    @BeforeEach
    public void init(){
        super.init();
        log.info("Initiating users in database");
        userService.saveUser(userMocks.u1());
        userService.saveUser(userMocks.u2());
    }

    @Override
    @AfterEach
    public void destroy(){
        log.info("Destroying users in database");
        userService.clear();
        super.destroy();
    }
}
