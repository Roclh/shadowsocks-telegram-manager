package org.Roclh.handlers.commands.telegramuser;

import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.telegramUser.ListTelegramUserCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.testutil.TelegramUserTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListTelegramUserCommandTest extends TelegramUserTestBase {

    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private ListTelegramUserCommand listTelegramUserCommand;

    private MessageData messageData;

    @BeforeEach
    public void init() {
        super.init();
        messageData = MessageData.builder()
                .telegramId(tgMocks.tgu1().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tgu1().getChatId()))
                .messageId(123456)
                .telegramName(Objects.requireNonNull(tgMocks.tgu1().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
    }

    @Test
    public void whenCorrectCommand_thenCorrectAnswer() {
        String command = "listtg {0}";
        List<TelegramUserModel> users = telegramUserService.getUsers(5, 0);
        long allUsersSize = telegramUserService.size();
        SendMessage result = listTelegramUserCommand.handle(CommandData.builder()
                .command(command)
                .messageData(messageData)
                .build());
        Assertions.assertEquals(allUsersSize + " telegram users from 0 to 5:\n" +
                users.stream().map(TelegramUserModel::toFormattedString)
                        .collect(Collectors.joining("\n")), result.getText());
    }


}
