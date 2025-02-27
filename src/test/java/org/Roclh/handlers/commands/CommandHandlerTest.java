package org.Roclh.handlers.commands;

import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.testutil.TelegramUserTestBase;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Locale;

public class CommandHandlerTest extends TelegramUserTestBase {

    private final MessageData messageData = MessageData.builder()
            .messageId(1)
            .telegramName("TestUser")
            .telegramId(12375L)
            .chatId(12375L)
            .locale(Locale.forLanguageTag("ru"))
            .build();

    private final CommandData testCommandData1 = CommandData.builder()
            .command("start")
            .messageData(messageData)
            .build();

    private final CommandData testCommandData2 = CommandData.builder()
            .command("some incorrect command")
            .messageData(messageData)
            .build();

    @Autowired
    private CommandHandler commandHandler;

    @Test
    public void whenCorrectCommandPassed_thenCorrectResult(){
        SendMessage sendMessage = (SendMessage) commandHandler.handleCommands(testCommandData1);
        Assertions.assertEquals(testCommandData1.getMessageData().getChatId(), Long.valueOf(sendMessage.getChatId()));
        Assertions.assertEquals(I18N.from(messageData).get("command.common.start.welcome.message"), sendMessage.getText());
    }

    @Test
    public void whenIncorrectCommandPassed_thenUnknownCommandError(){
        SendMessage sendMessage = (SendMessage) commandHandler.handleCommands(testCommandData2);
        Assertions.assertEquals(testCommandData1.getMessageData().getChatId(), Long.valueOf(sendMessage.getChatId()));
        Assertions.assertEquals("Unknown command", sendMessage.getText());
    }
}
