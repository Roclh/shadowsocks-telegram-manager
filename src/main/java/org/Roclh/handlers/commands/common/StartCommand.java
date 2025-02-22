package org.Roclh.handlers.commands.common;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Slf4j
@Component
public class StartCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final UserService userService;
    private final CommandRegistry commandRegistry;

    public StartCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, UserService userService) {
        super(telegramUserService, commandRegistry);
        this.commandRegistry = commandRegistry;
        this.userService = userService;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        MessageData messageData = commandData.getMessageData();
        long chatId = messageData.getChatId();
        SendMessage.SendMessageBuilder sendMessage = MessageUtils.sendMessage(messageData);
        if (telegramUserService.exists(messageData.getTelegramId()) && telegramUserService.isAllowed(messageData.getTelegramId(), Role.USER)) {
            sendMessage.text(telegramUserService.isAllowed(messageData.getTelegramId(), Role.MANAGER) ?
                    i18N.get("command.common.start.select.command",
                            messageData.getTelegramName(),
                            userService.getUser(messageData.getTelegramId()).map(UserModel::isEnabled).orElse(false) ?
                                    i18N.get("command.common.start.server.state.enabled") :
                                    i18N.get("command.common.start.server.state.disabled")) :
                    i18N.get("command.common.start.select.command.user",
                            messageData.getTelegramName(),
                            userService.getUser(messageData.getTelegramId()).map(UserModel::isEnabled).orElse(false) ?
                                    i18N.get("command.common.start.server.state.enabled") :
                                    i18N.get("command.common.start.server.state.disabled"))
            );
            sendMessage.replyMarkup(getInlineKeyboardButtons(commandData.getMessageData()));
        } else {
            telegramUserService.saveUser(TelegramUserModel.builder()
                    .role(Role.GUEST)
                    .telegramId(messageData.getTelegramId())
                    .telegramName(messageData.getTelegramName())
                    .chatId(chatId)
                    .build());
            sendMessage.text(i18N.get("command.common.start.welcome.message"));
            sendMessage.replyMarkup(getGuestKeyboardMarkup());
        }
        return sendMessage.build();
    }

    @Override
    public boolean isAllowed(Long userId) {
        return true;
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("start", "s");
    }

    public InlineKeyboardMarkup getGuestKeyboardMarkup() {
        return InlineUtils.getDefaultNavigationMarkup(i18N.get("command.common.start.register.button"), "register");
    }

    private InlineKeyboardMarkup getInlineKeyboardButtons(MessageData messageData) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(commandRegistry.getMergedRegisteredCallbacks(messageData.getTelegramId(), messageData.getLocale())
                .stream()
                .filter(callbackStack -> !callbackStack.isUtil())
                .map(CallbackStack::getCallbackStackButton)
                .toList());
        return keyboardMarkup;
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("start")
                .forCommand("start", "Старт")
                .util(true)
                .with(0, (callbackData) -> {
                            SendMessage result = handle(CommandData.from(callbackData, false));
                            return MessageUtils.editMessage(callbackData.getMessageData())
                                    .text(result.getText())
                                    .replyMarkup((InlineKeyboardMarkup) result.getReplyMarkup())
                                    .build();
                        }
                )
                .with(1, (callbackData) -> handle(CommandData.from(callbackData, false)))
                .build();
    }
}
