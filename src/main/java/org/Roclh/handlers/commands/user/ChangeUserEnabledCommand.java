package org.Roclh.handlers.commands.user;

import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.commands.AbstractCommand;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.sh.scripts.DisableShadowsocksServerScript;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.Roclh.sh.scripts.EnableV2RayShadowsocksServerScript;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.callback.CallbackStackUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ChangeUserEnabledCommand extends AbstractCommand<SendMessage> implements WithCallbackStack {
    private final List<String> enableCommands = List.of("enable");
    private final List<String> disableCommands = List.of("disable", "dis");

    private final UserService userService;
    private final EnableDefaultShadowsocksServerScript enableScript;
    private final EnableV2RayShadowsocksServerScript enableV2RayScript;
    private final DisableShadowsocksServerScript disableScript;

    public ChangeUserEnabledCommand(TelegramUserService telegramUserService,
                                    CommandRegistry commandRegistry,
                                    UserService userService,
                                    EnableDefaultShadowsocksServerScript enableScript,
                                    EnableV2RayShadowsocksServerScript enableV2RayScript,
                                    DisableShadowsocksServerScript disableScript) {
        super(telegramUserService, commandRegistry);
        this.userService = userService;
        this.enableScript = enableScript;
        this.enableV2RayScript = enableV2RayScript;
        this.disableScript = disableScript;
    }


    @Override
    public SendMessage handle(CommandData commandData) {
        String[] words = commandData.getCommand().split(" ");
        if (words.length < 2) {
            return MessageUtils.sendMessage(commandData.getMessageData()).text("Failed to execute command - not enough arguments").build();
        }
        String cmd = words[0];
        Long userId = Long.parseLong(words[1]);
        SendMessage.SendMessageBuilder sendMessage = MessageUtils.sendMessage(commandData.getMessageData());

        UserModel userModel = userService.getUser(userId).orElse(null);
        if (userModel == null) {
            sendMessage.text("User does not exists");
            return sendMessage.build();
        }

        boolean isEnabled = enableCommands.contains(cmd);
        if (changeEnabled(userModel, isEnabled)) {
            sendMessage.text("User was " + (isEnabled ? "enabled" : "disabled"));
            userModel.setEnabled(isEnabled);
            userService.saveUser(userModel);
        } else {
            sendMessage.text("User was not " + (isEnabled ? "enabled" : "disabled"));
        }
        return sendMessage.build();
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public List<String> getCommandNames() {
        return Stream.concat(enableCommands.stream(), disableCommands.stream()).collect(Collectors.toList());
    }

    public boolean changeEnabled(UserModel userModel, boolean enable) {
        if (enable) {
            return switch (userModel.getPlugin()) {
                case DEFAULT -> enableScript.execute(userModel);
                case V2RAY -> enableV2RayScript.execute(userModel);
            };
        } else {
            return disableScript.execute(userModel);
        }
    }

    @Override
    public CallbackStack getCallbackStack() {
        CallbackStack enableCallbackStack =
                CallbackStack.of("user")
                        .forCommand("enable",
                                i18N.get("callback.user.user.inline.button.enable.user")
                        )
                        .withCommandDisplayCondition((telegramId) -> userService.getAllUsers().stream().anyMatch(user -> !user.isEnabled()))
                        .with(1, (callbackData) ->
                                CallbackStackUtils.getDefaultSelectUserIdMessage(
                                        callbackData,
                                        i18N.get("callback.user.user.select.user.enable"),
                                        userService,
                                        user -> !user.isEnabled()
                                )
                        )
                        .with(2, (callbackData) -> MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build())
                        .build();
        CallbackStack disableCallbackStack =
                CallbackStack.of("user")
                        .forCommand("disable", i18N.get("callback.user.user.inline.button.disable.user"))
                        .withCommandDisplayCondition((telegramId) -> userService.getAllUsers().stream().anyMatch(UserModel::isEnabled))
                        .with(1, (callbackData) ->
                                CallbackStackUtils.getDefaultSelectUserIdMessage(
                                        callbackData,
                                        i18N.get("callback.user.user.select.user.disable"),
                                        userService,
                                        UserModel::isEnabled
                                )
                        )
                        .with(2, (callbackData) -> MessageUtils.editMessage(callbackData.getMessageData())
                                .text(handle(CommandData.from(callbackData)).getText())
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build())
                        .build();
        return enableCallbackStack.merge(disableCallbackStack);
    }
}
