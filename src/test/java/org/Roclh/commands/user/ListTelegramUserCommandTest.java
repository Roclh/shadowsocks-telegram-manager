package org.Roclh.commands.user;

import org.Roclh.data.entities.UserModel;
import org.Roclh.handlers.commands.user.ListCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public class ListTelegramUserCommandTest extends CommonUserCommandTest {

    @Autowired
    private ListCommand listCommand;

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    public void whenListCommand_thenListOfUsers() {
        Mockito.when(commandData.getCommand()).thenReturn("l");
        SendMessage sendMessage = listCommand.handle(commandData);
        List<UserModel> allUsers = userService.getAllUsers();
        commonSendMessageValidation(sendMessage, allUsers.size() + " added users:\n" +
                allUsers.stream().map(u-> u.toString() + bandwidthService.getRule(u.getUserModel().getTelegramId()).map(r -> "," + r).orElse(""))
                        .collect(Collectors.joining("\n")));
    }

}
