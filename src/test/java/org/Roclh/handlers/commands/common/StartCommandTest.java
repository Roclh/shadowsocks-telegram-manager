package org.Roclh.handlers.commands.common;

import org.Roclh.data.enums.Plugin;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.mock.UserMocks;
import org.Roclh.testutil.UserTestBase;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Locale;
import java.util.Objects;

public class StartCommandTest extends UserTestBase {


    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    private UserMocks uMocks;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private StartCommand startCommand;

    private MessageData rootMessageData;
    private MessageData userMessageData;
    private MessageData guestMessageData;

    @BeforeEach
    public void init() {
        super.init();
        rootMessageData = MessageData.builder()
                .telegramId(tgMocks.tguroot().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tguroot().getChatId()))
                .messageId(123456)
                .telegramName(Objects.requireNonNull(tgMocks.tguroot().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
        userMessageData = MessageData.builder()
                .telegramId(tgMocks.tgu1().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tgu1().getChatId()))
                .messageId(12345)
                .telegramName(Objects.requireNonNull(tgMocks.tgu1().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
        guestMessageData = MessageData.builder()
                .telegramId(tgMocks.tgNewU().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tgNewU().getChatId()))
                .messageId(1234)
                .telegramName(Objects.requireNonNull(tgMocks.tgNewU().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
    }

    @Test
    public void whenStartCommand_thenCorrectAnswer() {
        //root message
        I18N i18N = I18N.from(rootMessageData.getLocale());
        startCommand.setI18N(rootMessageData.getLocale());
        SendMessage rootResult = startCommand.handle(CommandData.builder()
                .command("start")
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(
                i18N.get("command.common.start.select.command",
                        rootMessageData.getTelegramName(),
                        i18N.get("command.common.start.server.state.disabled"),
                        Plugin.DEFAULT
                ),
                rootResult.getText()
        );
        //user message
        i18N = I18N.from(userMessageData.getLocale());
        startCommand.setI18N(userMessageData.getLocale());
        SendMessage userResult = startCommand.handle(CommandData.builder()
                .command("start")
                .messageData(userMessageData)
                .build());
        Assertions.assertEquals(
                i18N.get("command.common.start.select.command.user",
                        userMessageData.getTelegramName(),
                        uMocks.u1().isEnabled() ?
                                i18N.get("command.common.start.server.state.enabled") :
                                i18N.get("command.common.start.server.state.disabled"),
                        uMocks.u1().getPlugin()
                ),
                userResult.getText()
        );

        //new user message
        i18N = I18N.from(guestMessageData.getLocale());
        startCommand.setI18N(guestMessageData.getLocale());
        SendMessage guestResult = startCommand.handle(CommandData.builder()
                .command("start")
                .messageData(guestMessageData)
                .build());
        Assertions.assertEquals(i18N.get("command.common.start.welcome.message"), guestResult.getText());
        Assertions.assertTrue(telegramUserService.exists(tgMocks.tgNewU().getTelegramId()));
    }

    //TODO: Write tests for 2 args start command, callback stack command
}
