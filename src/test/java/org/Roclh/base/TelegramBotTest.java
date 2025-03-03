package org.Roclh.base;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBot;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.ShScriptsMocks;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.mock.UserMocks;
import org.Roclh.testutil.UserTestBase;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@Import(ShScriptsMocks.class)
public class TelegramBotTest extends UserTestBase {

    @Autowired
    @SpyBean
    private TelegramBot telegramBot;
    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    private UserMocks uMocks;

    private ArgumentCaptor<PartialBotApiMethod<? extends Serializable>> tgBotSendMessageCaptor;

    @BeforeEach
    public void init() {
        super.init();
        tgBotSendMessageCaptor = ArgumentCaptor.forClass(PartialBotApiMethod.class);
        Mockito.doNothing().when(telegramBot).sendMessage(Mockito.any());
    }

    @Test
    public void whenSentMessage_thenNextMessageHandledByCommandHandler() {
        Locale locale = Locale.forLanguageTag("ru");
        Update mockUpdate = mockMessageFrom(MessageData.builder()
                .telegramId(tgMocks.tgu1().getTelegramId())
                .telegramName(Objects.requireNonNull(tgMocks.tgu1().getTelegramName()))
                .messageId(123456)
                .chatId(Objects.requireNonNull(tgMocks.tgu1().getChatId()))
                .locale(locale)
                .build());
        when(mockUpdate.getMessage().getText()).thenReturn("start");
        telegramBot.onUpdateReceived(mockUpdate);
        verify(telegramBot, times(1)).sendMessage(tgBotSendMessageCaptor.capture());

        PartialBotApiMethod<? extends Serializable> capturedResult = tgBotSendMessageCaptor.getValue();
        Assertions.assertInstanceOf(SendMessage.class, capturedResult);

        I18N i18N = I18N.from(locale);
        SendMessage userResult = (SendMessage) capturedResult;
        Assertions.assertEquals(
                i18N.get("command.common.start.select.command.user",
                        tgMocks.tgu1().getTelegramName(),
                        uMocks.u1().isEnabled() ?
                                i18N.get("command.common.start.server.state.enabled") :
                                i18N.get("command.common.start.server.state.disabled"),
                        uMocks.u1().getPlugin()
                ),
                userResult.getText()
        );
    }
}
