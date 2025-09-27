package org.Roclh.handlers.commands.access;

import org.Roclh.bot.TelegramBotStorage;
import org.Roclh.data.enums.Role;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
public class RegisterCommand extends AbstractCommand<SendMessage> {
    public final TelegramBotStorage botStorage;

    public RegisterCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, TelegramBotStorage botStorage) {
        super(telegramUserService, commandRegistry);
        this.botStorage = botStorage;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        boolean isSaved = telegramUserService.saveUser(telegramUserService.getUser(commandData.getMessageData().getTelegramId())
                .map(user -> {
                    if (user.getTelegramName() == null) {
                        user.setTelegramName(commandData.getMessageData().getTelegramName());
                    }
                    if (user.getChatId() == null) {
                        user.setChatId(commandData.getMessageData().getChatId());
                    }
                    if (user.getRole().prior < Role.USER.prior) {
                        user.setRole(Role.USER);
                    }
                    return user;
                }).orElse(null));
        SendMessage.SendMessageBuilder sendMessage = MessageUtils.sendMessage(commandData.getMessageData());
        if (isSaved) {
            MessageData messageData = commandData.getMessageData();
            sendMessage.text(i18N.get("command.common.register.successfully.registred"));
            sendMessage.replyMarkup(InlineUtils.getDefaultNavigationMarkup(i18N.get("command.common.register.manage.button"), "start"));
            telegramUserService.getUsers(user -> user.getRole().prior >= Role.MANAGER.prior)
                    .filter(user -> user.getChatId() != null)
                    .forEach(user -> botStorage.getTelegramBot().sendMessage(SendMessage.builder()
                            .chatId(user.getChatId())
                            .text(i18N.get("command.common.register.notify.managers.message",
                                    messageData.getTelegramName(), messageData.getTelegramId()))
                            .replyMarkup(InlineUtils.getDefaultNavigationMarkup(i18N.get("command.common.register.start.managment.button"), "start"))
                            .build()));
        } else {
            sendMessage.text(i18N.get("command.common.register.already.registred"));
            sendMessage.replyMarkup(InlineUtils.getDefaultNavigationMarkup(i18N.get("command.common.register.manage.button"), "start"));
        }
        return sendMessage.build();
    }


    @Override
    public boolean isAllowed(Long userId) {
        return true;
    }

    @Override
    public String getHelp() {
        return getCommandNames().get(0) + "\n" + i18N.get("command.common.register.help");
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("register");
    }
}
