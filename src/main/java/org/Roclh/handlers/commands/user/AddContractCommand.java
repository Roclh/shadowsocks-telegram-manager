package org.Roclh.handlers.commands.user;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.entities.ContractModel;
import org.Roclh.data.services.ContractService;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.Roclh.utils.DateTimeUtils;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class AddContractCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final ContractService contractService;
    private final UserService userService;
    private final EnableDefaultShadowsocksServerScript enableDefaultShadowsocksServerScript;

    public AddContractCommand(TelegramUserService telegramUserService,
                              CommandRegistry commandRegistry,
                              ContractService contractService, UserService userService,
                              EnableDefaultShadowsocksServerScript enableDefaultShadowsocksServerScript) {
        super(telegramUserService, commandRegistry);
        this.contractService = contractService;
        this.userService = userService;
        this.enableDefaultShadowsocksServerScript = enableDefaultShadowsocksServerScript;
    }

    @Override
    public SendMessage handle(CommandData commandData) {
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 4) {
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("command.user.add.contract.validation.wrong.type")).build();
        }
        Long telegramId = Long.valueOf(words[1]);

        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        startDateTime = DateTimeUtils.parse(words[2]);
        endDateTime = DateTimeUtils.parse(words[3]);
        if (startDateTime == null || endDateTime == null) {
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("command.user.add.contract.validation.wrong.type")).build();
        }
        if (!userService.getUser(telegramId).map(user -> contractService
                .saveContract(ContractModel.builder().userModel(user)
                        .startDate(startDateTime)
                        .endDate(endDateTime)
                        .wasNotified(false)
                        .build())).orElse(false)) {
            log.error("Failed to save a contract for user with id {}", telegramId);
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("command.user.add.contract.validation.failed.to.save.contract", telegramId)).build();
        }
        if (!userService.getUser(telegramId).map(user -> {
            if (!user.isEnabled()) {
                user.setEnabled(true);
                userService.saveUser(user);
                return enableDefaultShadowsocksServerScript.execute(user);
            }
            return true;
        }).orElse(false)) {
            log.error("Failed to start shadowsocks server script for user with id {}", telegramId);
            return MessageUtils.sendMessage(commandData.getMessageData())
                    .text(i18N.get("command.user.add.contract.validation.failed.to.start.sh.script", telegramId)).build();
        }
        return MessageUtils.sendMessage(commandData.getMessageData())
                .text(i18N.get("command.user.add.contract.validation.successfully.save.contract", telegramId)).build();
    }

    @Override
    public List<String> getCommandNames() {
        return List.of("contract", "setContract", "contractset");
    }


    @Override
    public CallbackStack getCallbackStack() {
        return CallbackStack.of("user")
                .forCommand("contract", "Add contract")
                .with(1, (callbackData ) -> {
                    throw new RuntimeException("Not implemented");
                })
                .build();
    }
}
