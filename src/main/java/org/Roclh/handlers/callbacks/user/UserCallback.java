package org.Roclh.handlers.callbacks.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBot;
import org.Roclh.data.Role;
import org.Roclh.data.entities.BandwidthModel;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.PasswordUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserCallback extends AbstractCallback<PartialBotApiMethod<? extends Serializable>> {

    private final TelegramUserService telegramUserService;
    private final UserService userService;
    private final CommandHandler commandHandler;

    @Override
    public PartialBotApiMethod<? extends Serializable> apply(CallbackData callbackData) {
        MessageData messageData = callbackData.getMessageData();
        int commandLength = callbackData.getCallbackData().split(" ").length;
        return switch (commandLength) {
            case 1 -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Select command")
                    .replyMarkup(getSelectCommandMarkup(callbackData))
                    .build();
            case 2 -> handleOneArgumentCommand(callbackData);
            case 3 -> handleTwoArgumentCommand(callbackData);
            case 4 -> handleThreeArgumentCommand(callbackData);
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Failed to process inline navigation data")
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        };
    }

    @Override
    public boolean isAllowed(Long telegramId) {
        return telegramUserService.isAllowed(telegramId, Role.MANAGER);
    }

    @Override
    public List<InlineKeyboardButton> getCallbackButtonRow() {
        return List.of(InlineKeyboardButton.builder()
                .text("Manage users")
                .callbackData(getName())
                .build());
    }

    @Override
    public String getName() {
        return "user";
    }

    private PartialBotApiMethod<? extends Serializable> handleOneArgumentCommand(CallbackData callbackData) {
        MessageData messageData = callbackData.getMessageData();
        String command = callbackData.getCallbackData().split(" ")[1];
        return switch (command) {
            case "enable" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Select user id to enable")
                    .replyMarkup(getSelectUserIdMarkup(callbackData, user -> !user.isAdded()))
                    .build();
            case "disable" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Select user id to disable")
                    .replyMarkup(getSelectUserIdMarkup(callbackData, UserModel::isAdded))
                    .build();
            case "list" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(getSendMessageCommandResult(callbackData))
                    .replyMarkup(InlineUtils.combineKeyboardMarkups(
                            InlineUtils.getDefaultNavigationMarkup("Manage users", getName()),
                            InlineUtils.getNavigationToStart(callbackData.getMessageData())
                    ))
                    .build();
            case "addnopwd", "add" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Select user id to add")
                    .replyMarkup(getSelectTelegramUserIdMarkup(callbackData, user -> !userService.isAddedUser(user)))
                    .build();
            case "delete" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Select user id to delete")
                    .replyMarkup(getSelectUserIdMarkup(callbackData, user -> true))
                    .build();
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Failed to parse one argument command")
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        };
    }

    private PartialBotApiMethod<? extends Serializable> handleTwoArgumentCommand(CallbackData callbackData) {
        MessageData messageData = callbackData.getMessageData();
        String command = callbackData.getCallbackData().split(" ")[1];
        return switch (command) {
            case "addnopwd", "add" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Select port")
                    .replyMarkup(getSelectPortMarkup(callbackData))
                    .build();
            case "enable", "disable", "delete" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(getSendMessageCommandResult(callbackData))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Failed to parse two argument command")
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        };
    }

    private PartialBotApiMethod<? extends Serializable> handleThreeArgumentCommand(CallbackData callbackData) {
        MessageData messageData = callbackData.getMessageData();
        String command = callbackData.getCallbackData().split(" ")[1];
        return switch (command) {
            case "addnopwd" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(getSendMessageCommandResult(callbackData))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
            case "add" -> {
                TelegramBot.waitSyncUpdate(messageData.getTelegramId(), (commandData) -> {
                    if (PasswordUtils.validate(commandData.getCommand())) {
                        callbackData.setCallbackData(callbackData.getCallbackData() + " " + commandData.getCommand());
                        return MessageUtils.sendMessage(callbackData.getMessageData()).text(getSendMessageCommandResult(callbackData))
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build();
                    } else {
                        return MessageUtils.sendMessage(callbackData.getMessageData())
                                .text("Failed to validate password")
                                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                                .build();
                    }
                });
                yield MessageUtils.editMessage(callbackData.getMessageData())
                        .text("Write a new password")
                        .replyMarkup(InlineUtils.getDefaultNavigationMarkup(i18N.get("callback.default.navigation.data.back"), trimLastWord(callbackData.getCallbackData())))
                        .build();
            }
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Failed to parse two argument command")
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        };

    }

    private InlineKeyboardMarkup getSelectCommandMarkup(CallbackData callbackData) {
        Map<String, String> map = new HashMap<>();
        map.put("List of all users", "list");
        map.put("Add with gen password", "addnopwd");
        map.put("Add with defined password", "add");
        map.put("Delete user", "delete");
        if (userService.getAllUsers().stream().anyMatch(UserModel::isAdded)) {
            map.put("Disable user", "disable");
        }
        if (userService.getAllUsers().stream().anyMatch(user -> !user.isAdded())) {
            map.put("Enable user", "enable");
        }
        return InlineUtils.getListNavigationMarkup(map,
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getMessageData().getLocale(),
                () -> "start"
        );
    }

    private InlineKeyboardMarkup getSelectBandwidthMarkup(CallbackData callbackData) {
        return InlineUtils.getListNavigationMarkup(Arrays.stream(BandwidthModel.Bandwidth.values())
                        .collect(Collectors.toMap(BandwidthModel.Bandwidth::getBandwidth, BandwidthModel.Bandwidth::name)),
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getMessageData().getLocale(),
                () -> trimLastWord(callbackData.getCallbackData())
        );
    }

    private InlineKeyboardMarkup getSelectUserIdMarkup(CallbackData callbackData, Predicate<UserModel> filter) {
        return InlineUtils.getListNavigationMarkup(userService.getAllUsers()
                        .stream()
                        .filter(filter)
                        .collect(Collectors.toMap(user -> user.getUserModel().getTelegramName() + ":" + user.getUserModel().getTelegramId(),
                                userModel -> userModel.getUserModel().getTelegramId().toString())),
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getMessageData().getLocale(),
                () -> trimLastWord(callbackData.getCallbackData())
        );
    }

    private InlineKeyboardMarkup getSelectTelegramUserIdMarkup(CallbackData callbackData, Predicate<TelegramUserModel> filter) {
        return InlineUtils.getListNavigationMarkup(telegramUserService
                        .getUsers()
                        .stream()
                        .filter(filter)
                        .collect(Collectors.toMap(user -> user.getTelegramName() + ":" + user.getTelegramId(),
                                user -> user.getTelegramId().toString())),
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getMessageData().getLocale(),
                () -> trimLastWord(callbackData.getCallbackData())
        );
    }

    private InlineKeyboardMarkup getSelectPortMarkup(CallbackData callbackData) {
        return InlineUtils.getListNavigationMarkup(userService.getAvailablePorts(5)
                        .stream().collect(Collectors.toMap(port -> "Port " + port.toString(), Object::toString)),
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getMessageData().getLocale(),
                () -> trimLastWord(callbackData.getCallbackData())
        );
    }

    private String getSendMessageCommandResult(CallbackData callbackData) {
        CommandData commandData = CommandData.from(callbackData);
        String preparedCommand = commandData.getCommand().substring(commandData.getCommand().indexOf(" ") + 1);
        commandData.setCommand(preparedCommand);
        return ((SendMessage) commandHandler.handleCommands(commandData)).getText();
    }

    private String trimLastWord(String data) {
        return data.substring(0, data.lastIndexOf(" "));
    }
}
