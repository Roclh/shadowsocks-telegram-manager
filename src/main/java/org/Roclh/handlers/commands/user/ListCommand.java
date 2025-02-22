package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.BandwidthService;
import org.Roclh.data.services.ContractService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
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

@Slf4j
@Component
public class ListCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final int defaultPageSize = 5;
    private final UserService userService;
    private final BandwidthService bandwidthService;
    private final ContractService contractService;

    public ListCommand(TelegramUserService telegramUserService, CommandRegistry commandRegistry, UserService userService, BandwidthService bandwidthService, ContractService contractService) {
        super(telegramUserService, commandRegistry);
        this.userService = userService;
        this.bandwidthService = bandwidthService;
        this.contractService = contractService;
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
        List<UserModel> users = userService.getUsers(defaultPageSize, pageNumber);
        long allUsersSize = userService.size();
        return MessageUtils.sendMessage(commandData.getMessageData())
                .text("Shadowsocks users from " + (pageNumber * defaultPageSize) + " to " + (Math.min((long) (pageNumber + 1) * defaultPageSize, allUsersSize)) + " users:\n" +
                        users.stream().map(UserModel::toFormattedString)
                                .map(s -> bandwidthService.getRule(commandData.getMessageData().getTelegramId())
                                        .map(b -> s + b.toFormattedString()).orElse(s))
                                .map(s -> contractService.getContract(commandData.getMessageData().getTelegramId())
                                        .map(c -> s + c.toFormattedString()).orElse(s))
                                .collect(Collectors.joining("\n")))
                .build();
    }

    @Override
    public String getHelp() {
        return getCommandNames().get(0) + "\n -- show list of added users";
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("list", "l", "listusers");
    }

    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("user")
                .forCommand(getCommandNames().get(0), i18N.get("callback.user.user.inline.button.list.of.all.users"))
                .withSelectCommandText(i18N.get("callback.user.user.select.command"))
                .with(1, (callbackData) -> {
                    long userSize = userService.size();
                    if (!InlineUtils.paginationMatches(callbackData.getCallbackData())) {
                        callbackData.setCallbackData(callbackData.getCallbackData() + " {0}");
                    }
                    return MessageUtils.editMessage(callbackData.getMessageData())
                            .text(handle(CommandData.from(callbackData)).getText())
                            .replyMarkup(InlineUtils.combineKeyboardMarkups(
                                    InlineUtils.getListNavigationMarkup(callbackData,
                                            userSize / defaultPageSize + (userSize % defaultPageSize > 0 ? 1 : 0)
                                    ),
                                    InlineUtils.getNavigationToStart(callbackData.getMessageData())))
                            .build();
                })
                .with(2, (callbackData )-> {
                    long userSize = userService.size();
                    return MessageUtils.editMessage(callbackData.getMessageData())
                            .text(handle(CommandData.from(callbackData)).getText())
                            .replyMarkup(InlineUtils.combineKeyboardMarkups(
                                    InlineUtils.getListNavigationMarkup(callbackData,
                                            userSize / defaultPageSize + (userSize % defaultPageSize > 0 ? 1 : 0)
                                    ),
                                    InlineUtils.getNavigationToStart(callbackData.getMessageData())))
                            .build();
                })
                .build();
    }
}
