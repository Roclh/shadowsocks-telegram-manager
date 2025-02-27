package org.Roclh.handlers.commands.support;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HelpCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final CommandRegistry commandRegistry;

    public HelpCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry) {
        super(telegramUserService, commandRegistry);
        this.commandRegistry = commandRegistry;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        long chatId = messageData.getChatId();
        if (!telegramUserService.exists(messageData.getTelegramId())) {
            telegramUserService.saveUser(TelegramUserModel.builder()
                    .telegramId(messageData.getTelegramId())
                    .telegramName(messageData.getTelegramName())
                    .chatId(chatId)
                    .role(Role.GUEST)
                    .build());
        }
        return MessageUtils.sendMessage(messageData)
                .text(i18N.get("command.common.help.text") +
                        commandRegistry.getRegisteredCommands(messageData.getTelegramId())
                                .stream()
                                .map(command -> {
                                    command.setI18N(messageData.getLocale());
                                    return command.getHelp();
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining("\n\n")))
                .replyMarkup(getInlineKeyboardButtons(commandData.getMessageData()))
                .build();
    }

    @Override
    public boolean isAllowed(Long userId) {
        return telegramUserService.isAllowed(userId, Role.USER);
    }


    @Override
    public String getHelp() {
        return String.join("|", getCommandNames().subList(0, 2)) + "\n";
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("help", "h");
    }

    private InlineKeyboardMarkup getInlineKeyboardButtons(MessageData messageData) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(commandRegistry.getMergedRegisteredCallbacks(messageData.getTelegramId(), messageData.getLocale())
                .stream()
                .filter(callbackStack -> !callbackStack.isUtil())
                .map(callbackStack -> callbackStack.getCallbackStackButton(messageData.getTelegramId()))
                .filter(Objects::nonNull)
                .toList());
        return keyboardMarkup;
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("support")
                .forCommand("help", EmojiConstants.BOOK + " " + i18N.get("command.support.commands.list.inline.button"))
                .withSelectCommandText(i18N.get("command.support.select.command.text"))
                .withLocalizedCallbackKey(EmojiConstants.QUESTIONMARK + i18N.get("callback.common.help.inline.button"))
                .with(1, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build())
                .build();
    }
}
