package org.Roclh.handlers.commands.telegramUser;

import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListTelegramUserCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {

    public ListTelegramUserCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry) {
        super(telegramUserService, commandRegistry);
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        List<TelegramUserModel> allUsers = telegramUserService.getUsers();
        return MessageUtils.sendMessage(commandData.getMessageData())
                .text(allUsers.size() + " telegram users:\n" +
                        allUsers.stream().map(TelegramUserModel::toFormattedString)
                                .collect(Collectors.joining("\n")))
                .build();
    }


    @Override
    public String getHelp() {
        return getCommandNames().get(0) + "\n -- show full list of users";
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("listtg", "ltg");
    }


    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("tguser")
                .forCommand("listtg", i18N.get("callback.user.telegramuser.inline.button.list.of.telegram.users"))
                .withSelectCommandText(i18N.get("callback.user.telegramuser.select.command"))
                .withLocalizedCallbackKey(i18N.get("callback.user.telegramuser.callback.button"))
                .with(1, (callbackData) ->
                        MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.combineKeyboardMarkups(
                                        InlineUtils.getDefaultNavigationMarkup(i18N.get("callback.user.telegramuser.callback.button"), "tguser"),
                                        InlineUtils.getNavigationToStart(callbackData.getMessageData())
                                ))
                                .build()
                ).build();
    }
}
