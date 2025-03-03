package org.Roclh.handlers.callbacks.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.enums.Bandwidth;
import org.Roclh.data.enums.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Slf4j
@Component
@Deprecated
@RequiredArgsConstructor
public class UserCallback extends AbstractCallback<PartialBotApiMethod<? extends Serializable>> {

    private final TelegramUserService telegramUserService;
    private final UserService userService;
    private final CommandHandler commandHandler;

    @Override
    public PartialBotApiMethod<? extends Serializable> apply(CallbackData callbackData) {
        int commandLength = callbackData.getCallbackData().split(" ").length;
        if (InlineUtils.paginationMatches(callbackData.getCallbackData())) {
            commandLength -= 1;
        }
        return switch (commandLength) {
            case 1 -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text("Select command")
                    .replyMarkup(getSelectCommandMarkup(callbackData))
                    .build();
            case 2 -> handleOneArgumentCommand(callbackData);
            case 3 -> handleTwoArgumentCommand(callbackData);
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.default.navigation.data.error"))
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
                .text(i18N.get("callback.user.user.inline.button.manage.users"))
                .callbackData(getName())
                .build());
    }

    @Override
    public String getName() {
        return "user";
    }

    private PartialBotApiMethod<? extends Serializable> handleOneArgumentCommand(CallbackData callbackData) {
        String command = callbackData.getCallbackData().split(" ")[1];
        return switch (command) {
            case "contract" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.user.user.select.user.add"))
                    .replyMarkup(getSelectTelegramUserIdMarkup(callbackData, user -> !userService.isAddedUser(user)))
                    .build();
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.default.navigation.data.error.parse.one.argument"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        };
    }

    private PartialBotApiMethod<? extends Serializable> handleTwoArgumentCommand(CallbackData callbackData) {
        String command = callbackData.getCallbackData().split(" ")[1];
        return switch (command) {
            case "lflow" -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(getSendMessageCommandResult(callbackData))
                    .replyMarkup(getSelectBandwidthMarkup(callbackData))
                    .build();
            default -> MessageUtils.editMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.default.navigation.data.error.parse.two.argument"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        };
    }

    private InlineKeyboardMarkup getSelectCommandMarkup(CallbackData callbackData) {
        Map<String, String> map = new LinkedHashMap<>();
        if (userService.getAllUsers().stream().anyMatch(UserModel::isEnabled)) {
            map.put("Set contract", "contract");
        }
        return InlineUtils.getListNavigationMarkup(map,
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getMessageData().getLocale(),
                () -> "start"
        );
    }

    private InlineKeyboardMarkup getSelectBandwidthMarkup(CallbackData callbackData) {
        return InlineUtils.getListNavigationMarkup(Arrays.stream(Bandwidth.values())
                        .collect(Collectors.toMap(Bandwidth::getBandwidth, Bandwidth::name)),
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

    private String getSendMessageCommandResult(CallbackData callbackData) {
        CommandData commandData = CommandData.from(callbackData);
        String preparedCommand = commandData.getCommand().substring(commandData.getCommand().indexOf(" ") + 1);
        commandData.setCommand(preparedCommand);
        return ((SendMessage) commandHandler.handleCommands(commandData)).getText();
    }

}
