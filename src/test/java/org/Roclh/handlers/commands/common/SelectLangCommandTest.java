package org.Roclh.handlers.commands.common;

import org.Roclh.bot.TelegramBotProperties;
import org.Roclh.data.services.LocalizationService;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.testutil.TelegramUserTestBase;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Locale;
import java.util.Objects;

public class SelectLangCommandTest extends TelegramUserTestBase {
    @Autowired
    private SelectLangCommand selectLangCommand;
    @Autowired
    private TelegramBotProperties telegramBotProperties;
    @Autowired
    private TelegramUserMocks tgUMocks;
    @Autowired
    private LocalizationService localizationService;

    private MessageData messageData;

    @BeforeEach
    public void init() {
        super.init();
        messageData = MessageData.builder()
                .telegramId(tgUMocks.tgu1().getTelegramId())
                .chatId(Objects.requireNonNull(tgUMocks.tgu1().getChatId()))
                .messageId(12345)
                .telegramName(Objects.requireNonNull(tgUMocks.tgu1().getTelegramName()))
                .locale(localizationService.getOrCreate(tgUMocks.tgu1().getTelegramId()))
                .build();
    }

    @Test
    public void whenCorrectCommand_thenLanguageChanged() {
        String command = "lang " + telegramBotProperties.getSupportedLocales().get(1);
        I18N i18N = I18N.from(messageData.getLocale());
        selectLangCommand.setI18N(messageData.getLocale());
        SendMessage result = selectLangCommand.handle(CommandData.builder()
                .command(command)
                .messageData(messageData)
                .build());
        Assertions.assertEquals(i18N.get("command.common.selectlang.success", i18N.get(telegramBotProperties.getSupportedLocales().get(1))),
                result.getText());
        Assertions.assertAll(
                () -> Assertions.assertTrue(localizationService.exists(tgUMocks.tgu1().getTelegramId())),
                () -> Assertions.assertEquals(localizationService.getOrCreate(tgUMocks.tgu1().getTelegramId()),
                        Locale.forLanguageTag(telegramBotProperties.getSupportedLocales().get(1)))
        );
    }

    //TODO: Write test for all validations of selectLangCommand
}
