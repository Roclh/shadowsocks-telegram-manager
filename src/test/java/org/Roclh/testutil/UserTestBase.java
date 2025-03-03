package org.Roclh.testutil;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.UserService;
import org.Roclh.mock.ShScriptsMocks;
import org.Roclh.mock.UserMocks;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Slf4j
@Import(ShScriptsMocks.class)
public abstract class UserTestBase extends TelegramUserTestBase {

    @Autowired
    private UserMocks userMocks;
    @Autowired
    private UserService userService;
    @Autowired
    private EnableDefaultShadowsocksServerScript enableDefaultShadowsocksServerScript;

    @Override
    @BeforeEach
    public void init(){
        super.init();
        log.info("Initiating users in database");
        userService.saveUser(userMocks.u1());
        userService.saveUser(userMocks.u2());
        enableDefaultShadowsocksServerScript.execute(userMocks.u1());
    }

    @Override
    @AfterEach
    public void destroy(){
        log.info("Destroying users in database");
        userService.clear();
        super.destroy();
    }
}
