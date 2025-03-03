package org.Roclh.handlers.commands.access;

import org.Roclh.bot.TelegramBot;
import org.Roclh.data.enums.Role;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.testutil.TelegramUserTestBase;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class RegisterCommandTest extends TelegramUserTestBase {
    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private RegisterCommand registerCommand;
    @Autowired
    @SpyBean
    private TelegramBot telegramBot;
    private MessageData guestMessageData;
    private ArgumentCaptor<PartialBotApiMethod<? extends Serializable>> tgBotSendMessageCaptor = ArgumentCaptor.forClass(PartialBotApiMethod.class);

    @BeforeEach
    public void init() {
        super.init();
        guestMessageData = MessageData.builder()
                .telegramId(tgMocks.tgu2().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tgu2().getTelegramId()))
                .messageId(123335)
                .telegramName(Objects.requireNonNull(tgMocks.tgu2().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
        Mockito.doNothing().when(telegramBot).sendMessage(Mockito.any());
    }

    @Test
    public void whenCorrectCommand_thenUserRoleIsUser() {
        String command = "register";
        registerCommand.setI18N(guestMessageData.getLocale());
        SendMessage result = registerCommand.handle(CommandData.builder()
                .messageData(guestMessageData)
                .command(command)
                .build());
        Assertions.assertEquals(I18N.from(guestMessageData.getLocale()).get("command.common.register.successfully.registred"),
                result.getText());
        Assertions.assertAll(
                () -> Assertions.assertTrue(telegramUserService.exists(guestMessageData.getTelegramId())),
                () -> Assertions.assertTrue(telegramUserService.getUser(guestMessageData.getTelegramId())
                        .map(user -> user.getRole().equals(Role.USER)).orElse(false))
        );
        Mockito.verify(telegramBot, Mockito.atLeast(1)).sendMessage(tgBotSendMessageCaptor.capture());
        Assertions.assertTrue(tgBotSendMessageCaptor.getAllValues().stream().anyMatch(message -> {
            if (message instanceof SendMessage) {
                return ((SendMessage) message).getText().equals(I18N.from(Locale.forLanguageTag("ru")).get("command.common.register.notify.managers.message",
                        guestMessageData.getTelegramName(), guestMessageData.getTelegramId()));
            }
            return false;
        }));
    }


    //TODO: Add tests for all validations
}
