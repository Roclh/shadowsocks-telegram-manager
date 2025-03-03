package org.Roclh.handlers.commands.common;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBotProperties;
import org.Roclh.data.enums.Role;
import org.Roclh.data.services.LocalizationService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.callback.CallbackStackUtils;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Slf4j
@Component
public class SelectLangCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final LocalizationService localizationService;
    private final TelegramBotProperties telegramBotProperties;

    public SelectLangCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, LocalizationService localizationService, TelegramBotProperties telegramBotProperties) {
        super(telegramUserService, commandRegistry);
        this.localizationService = localizationService;
        this.telegramBotProperties = telegramBotProperties;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        String[] command = commandData.getCommand().split(" ");
        if (command.length < 2) {
            log.error("Failed to execute command - not enough arguments");
            return MessageUtils.sendMessage(messageData).text(i18N.get("common.validation.not.enough.argument", 2))
                    .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData())).build();
        }
        String locale = command[1];
        if (!telegramBotProperties.getSupportedLocales().contains(locale)) {
            log.error("Failed to execute command - not supported locale {}", locale);
            return MessageUtils.sendMessage(messageData).text(i18N.get("command.common.selectlang.validation.not.supported.locale", locale))
                    .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData())).build();
        }
        if (!localizationService.setLocale(messageData.getTelegramId(), locale)) {
            log.error("Failed to execute command - either failed for user with id {} to change locale to {}",
                    messageData.getTelegramId(),
                    locale);
            return MessageUtils.sendMessage(messageData).text(i18N.get("command.common.selectlang.validation.failed.to.change.locale",
                            messageData.getTelegramId(),
                            locale))
                    .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData())).build();
        }
        log.info("Successfully changed locale for user with id {}", messageData.getTelegramId());
        setI18N(localizationService.getOrCreate(messageData.getTelegramId()));
        return MessageUtils.sendMessage(messageData)
                .text(i18N.get("command.common.selectlang.success", i18N.get(locale)))
                .build();
    }

    @Override
    public boolean isAllowed(Long userId) {
        return telegramUserService.isAllowed(userId, Role.USER);
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("lang", "chlang", "locale", "chlocale");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("lang")
                .forCommand("lang", "Выбрать язык")
                .withLocalizedCallbackKey(EmojiConstants.GLOBE + i18N.get("callback.common.selectlang.select.button.inline"))
                .withSelectCommandText("Выберите язык")
                .with(0, (callbackData) ->
                        CallbackStackUtils.getDefaultSelectLangMessage(callbackData, telegramBotProperties.getSupportedLocales())
                )
                .with(1, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData, false)).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build()
                )
                .build();
    }
}
