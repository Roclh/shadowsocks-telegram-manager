package org.Roclh.handlers.commands.telegramUser;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.i18n.EmojiConstants;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ListTelegramUserCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final int defaultPageSize = 5;

    public ListTelegramUserCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry) {
        super(telegramUserService, commandRegistry);
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 2) {
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("common.validation.not.enough.argument", 2))
                    .replyMarkup(InlineUtils.getNavigationToStart(commandData.getMessageData()))
                    .build();
        }
        int pageNumber = 0;
        try {
            pageNumber = InlineUtils.getPageNumber(commandData.getCommand());
        } catch (NumberFormatException e) {
            log.error(i18N.get("command.user.list.validation.page.parse", words[1]));
        }
        List<TelegramUserModel> users = telegramUserService.getUsers(defaultPageSize, pageNumber);
        long allUsersSize = telegramUserService.size();
        return MessageUtils.sendMessage(commandData.getMessageData())
                .text(allUsersSize + " telegram users from " + (pageNumber * defaultPageSize) + " to " + (Math.min((long) (pageNumber + 1) * defaultPageSize, allUsersSize)) + ":\n" +
                        users.stream().map(TelegramUserModel::toFormattedString)
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
                .forCommand("listtg", EmojiConstants.CLIPBOARD + " " + i18N.get("callback.user.telegramuser.inline.button.list.of.telegram.users"))
                .withSelectCommandText(i18N.get("callback.user.telegramuser.select.command"))
                .withLocalizedCallbackKey(EmojiConstants.WRENCH + " " + i18N.get("callback.user.telegramuser.callback.button"))
                .with(1, (callbackData) -> {
                    long userSize = telegramUserService.size();
                    if (!InlineUtils.paginationMatches(callbackData.getCallbackData())) {
                        callbackData.setCallbackData(callbackData.getCallbackData() + " {0}");
                    }
                    return MessageUtils.editMessage(callbackData.getMessageData())
                            .text(handle(CommandData.from(callbackData)).getText())
                            .replyMarkup(InlineUtils.combineKeyboardMarkups(
                                    InlineUtils.getListNavigationMarkup(callbackData,
                                            userSize / defaultPageSize + (userSize % defaultPageSize > 0 ? 1 : 0)
                                    ),
                                    InlineUtils.getDefaultNavigationMarkup(i18N.get("callback.user.telegramuser.callback.button"), "tguser"),
                                    InlineUtils.getNavigationToStart(callbackData.getMessageData())
                            ))
                            .build();
                })
                .build();
    }
}
