package org.Roclh.commands.telegramuser;

import org.Roclh.bot.TelegramBotProperties;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.telegramUser.DeleteTelegramUserCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.testutil.TelegramUserTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Locale;
import java.util.Objects;

public class DeleteTelegramUserCommandTest extends TelegramUserTestBase {

    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private DeleteTelegramUserCommand deleteTelegramUserCommand;
    @Autowired
    private TelegramBotProperties telegramBotProperties;

    private MessageData messageData;

    @BeforeEach
    public void init(){
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
    public void whenDeleteExistingUser_thenUserRemoved() {
        String command = "deltg " + tgMocks.tgu1().getTelegramId();
        SendMessage sendMessage = deleteTelegramUserCommand.handle(
                CommandData.builder()
                        .command(command)
                        .messageData(messageData)
                        .build()
        );
        Assertions.assertEquals("Telegram user with identifier " + tgMocks.tgu1().getTelegramId() + " was deleted successfully!",
                sendMessage.getText());
        Assertions.assertFalse(telegramUserService.exists(tgMocks.tgu1().getTelegramId()));
    }

    @Test
    public void whenDeleteNonExistingUser_thenErrorMessage() {
        long nonExistingId = 12345678L;
        String command = "deltg " + nonExistingId;
        SendMessage sendMessage = deleteTelegramUserCommand.handle(
                CommandData.builder()
                        .command(command)
                        .messageData(messageData)
                        .build()
        );
        Assertions.assertEquals("Failed to delete telegram user with identifier " + nonExistingId, sendMessage.getText());
        Assertions.assertFalse(telegramUserService.exists(nonExistingId));
    }

    @Test
    public void whenDeleteWrongAmountOfArgs_thenValidationErrorMessage() {
        String command = "deltg";
        SendMessage sendMessage = deleteTelegramUserCommand.handle(
                CommandData.builder()
                        .command(command)
                        .messageData(messageData)
                        .build()
        );
        Assertions.assertEquals("Failed to execute command - not enough arguments", sendMessage.getText());
    }

    @Test
    public void whenDeleteDefaultManager_thenErrorMessage(){
        String command = "deltg " + telegramBotProperties.getDefaultManagerId();
        SendMessage sendMessage = deleteTelegramUserCommand.handle(
                CommandData.builder()
                        .command(command)
                        .messageData(messageData)
                        .build()
        );
        Assertions.assertEquals("Can't delete default manager", sendMessage.getText());
        Assertions.assertTrue(telegramUserService.exists(telegramBotProperties.getDefaultManagerId()));
    }

}
