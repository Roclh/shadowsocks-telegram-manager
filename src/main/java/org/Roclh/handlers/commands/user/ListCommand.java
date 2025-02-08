package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.BandwidthService;
import org.Roclh.data.services.ContractService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ListCommand extends AbstractCommand<SendMessage> {
    private final UserService userService;
    private final BandwidthService bandwidthService;
    private final ContractService contractService;

    public ListCommand(TelegramUserService telegramUserService, UserService userService, BandwidthService bandwidthService, ContractService contractService) {
        super(telegramUserService);
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
            pageNumber = InlineUtils.getPageNumber(words[1]);
        } catch (NumberFormatException e) {
            log.error(i18N.get("command.user.list.validation.page.parse", words[1]));
        }
        final int defaultPageSize = 2;
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
}
