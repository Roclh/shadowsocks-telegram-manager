package org.Roclh.handlers.callbacks.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.CommandHandler;
import org.Roclh.handlers.callbacks.AbstractCallback;
import org.Roclh.handlers.callbacks.CallbackData;
import org.Roclh.handlers.commands.CommandData;
import org.Roclh.utils.InlineUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
public class TelegramUserCallback extends AbstractCallback<PartialBotApiMethod<? extends Serializable>> {
    private final CommandHandler commandHandler;
    private final TelegramUserService telegramUserService;

    @Override
    public PartialBotApiMethod<? extends Serializable> apply(CallbackData callbackData) {
        int commandLength = callbackData.getCallbackData().split(" ").length;
        if (InlineUtils.paginationMatches(callbackData.getCallbackData())) {
            commandLength -= 1;
        }
        return switch (commandLength) {
            case 1 -> EditMessageText.builder()
                    .messageId(callbackData.getMessageId())
                    .text(i18N.get("callback.user.telegramuser.select.command"))
                    .chatId(callbackData.getChatId())
                    .replyMarkup(getSelectCommandMarkup(callbackData))
                    .build();
            case 2 -> handleOneArgumentCommand(callbackData);
            case 3 -> handleTwoArgumentCommand(callbackData);
            case 4 -> handleThreeArgumentCommand(callbackData);
            default -> SendMessage.builder()
                    .text(i18N.get("callback.default.navigation.data.error"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData))
                    .chatId(callbackData.getChatId()).build();
        };
    }

    @Override
    public String getName() {
        return "tguser";
    }

    @Override
    public boolean isAllowed(Long telegramId) {
        return telegramUserService.isAllowed(telegramId, Role.MANAGER);
    }

    @Override
    public List<InlineKeyboardButton> getCallbackButtonRow() {
        return List.of(InlineKeyboardButton.builder()
                .text(i18N.get("callback.user.telegramuser.callback.button"))
                .callbackData(getName())
                .build());
    }

    private PartialBotApiMethod<? extends Serializable> handleOneArgumentCommand(CallbackData callbackData) {
        String command = callbackData.getCallbackData().split(" ")[1];
        return switch (command) {
            case "listtg" -> EditMessageText.builder()
                    .messageId(callbackData.getMessageId())
                    .text(getSendMessageCommandResult(callbackData))
                    .chatId(callbackData.getChatId())
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData))
                    .build();
            case "deltg" -> EditMessageText.builder()
                    .messageId(callbackData.getMessageId())
                    .text(i18N.get("callback.user.telegramuser.select.telegram.user.delete"))
                    .chatId(callbackData.getChatId())
                    .replyMarkup(getSelectTelegramUserIdMarkup(callbackData, user -> true))
                    .build();
            case "setrole" -> EditMessageText.builder()
                    .messageId(callbackData.getMessageId())
                    .text(i18N.get("callback.user.telegramuser.select.telegram.user.setrole"))
                    .chatId(callbackData.getChatId())
                    .replyMarkup(getSelectTelegramUserIdMarkup(callbackData, user -> true))
                    .build();
            default -> SendMessage.builder()
                    .chatId(callbackData.getChatId())
                    .text(i18N.get("callback.default.navigation.data.error.parse.one.argument"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData))
                    .build();
        };
    }

    private PartialBotApiMethod<? extends Serializable> handleTwoArgumentCommand(CallbackData callbackData) {
        String command = callbackData.getCallbackData().split(" ")[1];
        return switch (command) {
            case "deltg" -> EditMessageText.builder()
                    .messageId(callbackData.getMessageId())
                    .text(getSendMessageCommandResult(callbackData))
                    .chatId(callbackData.getChatId())
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData))
                    .build();
            case "setrole" -> EditMessageText.builder()
                    .messageId(callbackData.getMessageId())
                    .text(i18N.get("callback.user.telegramuser.select.role"))
                    .chatId(callbackData.getChatId())
                    .replyMarkup(getSelectRoleMarkup(callbackData))
                    .build();
            default -> SendMessage.builder()
                    .chatId(callbackData.getChatId())
                    .text(i18N.get("callback.default.navigation.data.error.parse.two.argument"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData))
                    .build();
        };
    }

    private PartialBotApiMethod<? extends Serializable> handleThreeArgumentCommand(CallbackData callbackData) {
        String command = callbackData.getCallbackData().split(" ")[1];
        return switch (command) {
            case "setrole" -> EditMessageText.builder()
                    .messageId(callbackData.getMessageId())
                    .text(getSendMessageCommandResult(callbackData))
                    .chatId(callbackData.getChatId())
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData))
                    .build();
            default -> SendMessage.builder()
                    .chatId(callbackData.getChatId())
                    .text(i18N.get("callback.default.navigation.data.error.parse.three.argument"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData))
                    .build();
        };
    }


    private InlineKeyboardMarkup getSelectCommandMarkup(CallbackData callbackData) {
        Map<String, String> map = new HashMap<>();
        map.put(i18N.get("callback.user.telegramuser.inline.button.list.of.telegram.users"), "listtg");
        map.put(i18N.get("callback.user.telegramuser.inline.button.delete.telegram.user"), "deltg");
        map.put(i18N.get("callback.user.telegramuser.inline.button.set.role"), "setrole");
        return InlineUtils.getListNavigationMarkup(map,
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getLocale(),
                () -> "start"
        );
    }

    private InlineKeyboardMarkup getSelectRoleMarkup(CallbackData callbackData) {
        String[] command = callbackData.getCallbackData().split(" ");
        Assert.isTrue(command.length >= 3, "Select role expects to have at least command and 2 arguments");
        Long selectedId = Long.parseLong(command[command.length - 1]);
        return InlineUtils.getListNavigationMarkup(Arrays.stream(Role.values())
                        .filter(role -> telegramUserService.getUser(selectedId).map(user -> !user.getRole().equals(role)).orElse(false))
                        .filter(role -> telegramUserService.isAllowed(callbackData.getTelegramId(), role))
                        .collect(Collectors.toMap(Role::name, Role::name)),
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getLocale(),
                () -> callbackData.getCallbackData().substring(0, callbackData.getCallbackData().lastIndexOf(" "))
        );
    }


    private InlineKeyboardMarkup getSelectTelegramUserIdMarkup(CallbackData callbackData, Predicate<TelegramUserModel> filter) {
        if (!InlineUtils.paginationMatches(callbackData.getCallbackData())) {
            callbackData.setCallbackData(callbackData.getCallbackData() + " {0}");
        }
        log.info("Setting up a telegram user id markup with command {}", callbackData.getCallbackData());
        return InlineUtils.getListNavigationMarkupWithPagination(telegramUserService
                        .getUsers()
                        .stream()
                        .filter(filter)
                        .collect(Collectors.toMap(user -> user.getTelegramName() + ":" + user.getTelegramId(),
                                user -> user.getTelegramId().toString())),
                (data) -> InlineUtils.replace(callbackData, data),
                callbackData,
                () ->{
                    String callback = callbackData.getCallbackData().substring(0, callbackData.getCallbackData().lastIndexOf(" "));
                    return callback.substring(0, callback.lastIndexOf(" "));
                },
                5
        );
    }

    private String getSendMessageCommandResult(CallbackData callbackData) {
        CommandData commandData = CommandData.from(callbackData);
        String preparedCommand = commandData.getCommand().substring(commandData.getCommand().indexOf(" ") + 1);
        commandData.setCommand(preparedCommand);
        return ((SendMessage) commandHandler.handleCommands(commandData)).getText();
    }
}
