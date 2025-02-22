package org.Roclh.data.user;

import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.UserService;
import org.Roclh.mock.UserMocks;
import org.Roclh.testutil.UserTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UserServiceTest extends UserTestBase {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMocks userMocks;

    @Test
    public void testUsersExists() {
        List<UserModel> userModels = userService.getAllUsers();
        Assertions.assertAll(() -> Assertions.assertTrue(userModels.stream().anyMatch(
                        user -> user.getUserModel().getTelegramId().equals(userMocks.u1().getUserModel().getTelegramId()))),
                () -> Assertions.assertTrue(userModels.stream().anyMatch(
                        user -> user.getUserModel().getTelegramId().equals(userMocks.u2().getUserModel().getTelegramId())))
        );
    }
}
