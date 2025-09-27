package org.Roclh.utils.callback;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.bot.TelegramBot;
import org.Roclh.data.enums.Bandwidth;
import org.Roclh.data.enums.Plugin;
import org.Roclh.data.enums.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.PasswordUtils;
import org.Roclh.utils.i18n.I18N;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class CallbackStackUtils {

    public static EditMessageText getDefaultSelectPortMessage(CallbackData callbackData, String portMessage, UserService userService) {
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(portMessage)
                .replyMarkup(InlineUtils.getListNavigationMarkup(userService.getAvailablePorts(5)
                                .stream().collect(Collectors.toMap(port -> "Port " + port.toString(), Object::toString)),
                        (data) -> callbackData.getCallbackData() + " " + data,
                        callbackData.getMessageData().getLocale(),
                        () -> InlineUtils.trimLastWord(callbackData.getCallbackData())
                ))
                .build();
    }

    public static EditMessageText getDefaultSelectTelegramUserIdMessage(CallbackData callbackData, String userMessage, TelegramUserService telegramUserService, Predicate<TelegramUserModel> filter) {
        if (!InlineUtils.paginationMatches(callbackData.getCallbackData())) {
            callbackData.setCallbackData(callbackData.getCallbackData() + " {0}");
        }
        log.info("Setting up a telegram user id markup with command {}", callbackData.getCallbackData());
        InlineKeyboardMarkup markup = InlineUtils.getListNavigationMarkupWithPagination(telegramUserService
                        .getUsers()
                        .stream()
                        .filter(filter)
                        .collect(Collectors.toMap(user -> user.getTelegramName() + ":" + user.getTelegramId(),
                                user -> user.getTelegramId().toString())),
                (data) -> InlineUtils.replacePage(callbackData, data),
                callbackData,
                () -> InlineUtils.trimLastWord(InlineUtils.trimLastWord(callbackData.getCallbackData())),
                5
        );
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(userMessage)
                .replyMarkup(markup)
                .build();
    }

    public static EditMessageText getDefaultSelectUserIdMessage(CallbackData callbackData, String userMessage, UserService userService, Predicate<UserModel> filter) {
        if (!InlineUtils.paginationMatches(callbackData.getCallbackData())) {
            callbackData.setCallbackData(callbackData.getCallbackData() + " {0}");
        }
        log.info("Setting up a VPN user id markup with command {}", callbackData.getCallbackData());
        InlineKeyboardMarkup markup = InlineUtils.getListNavigationMarkupWithPagination(userService
                        .getAllUsers()
                        .stream()
                        .filter(filter)
                        .collect(Collectors.toMap(user -> user.getUserModel().getTelegramName() + ":" + user.getUserModel().getTelegramId(),
                                user -> user.getUserModel().getTelegramId().toString())),
                (data) -> InlineUtils.replacePage(callbackData, data),
                callbackData,
                () -> InlineUtils.trimLastWord(InlineUtils.trimLastWord(callbackData.getCallbackData())),
                5
        );
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(userMessage)
                .replyMarkup(markup)
                .build();
    }

    public static EditMessageText getDefaultSelectRoleMessage(CallbackData callbackData, Role selectedRole, String userMesage, TelegramUserService telegramUserService) {
        MessageData messageData = callbackData.getMessageData();
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(userMesage)
                .replyMarkup(InlineUtils.getListNavigationMarkup(Arrays.stream(Role.values())
                                .filter(role -> !selectedRole.equals(role))
                                .filter(role -> telegramUserService.isAllowed(messageData.getTelegramId(), role))
                                .collect(Collectors.toMap(Role::name, Role::name)),
                        (data) -> callbackData.getCallbackData() + " " + data,
                        messageData.getLocale(),
                        () -> InlineUtils.trimLastWord(callbackData.getCallbackData())
                ))
                .build();
    }

    public static EditMessageText getDefaultSelectLangMessage(CallbackData callbackData, List<String> supportedLocales) {
        I18N i18N = I18N.from(callbackData.getMessageData());
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(i18N.get("callback.common.selectlang.select.lang.message"))
                .replyMarkup(InlineUtils.getListNavigationMarkup(supportedLocales
                                .stream().collect(Collectors.toMap(i18N::get, lang -> lang)),
                        (data) -> callbackData.getCallbackData() + " " + data,
                        callbackData.getMessageData().getLocale(),
                        () -> "start"
                ))
                .build();
    }

    public static PartialBotApiMethod<? extends Serializable> getDefaultWaitForPasswordInput(CallbackData callbackData, CallbackStack callbackStack) {
        I18N i18N = I18N.from(callbackData.getMessageData());
        TelegramBot.waitSyncUpdate(callbackData.getMessageData().getTelegramId(), (commandData) -> {
            if (PasswordUtils.validate(commandData.getCommand())) {
                callbackData.setCallbackData(callbackData.getCallbackData() + " " + commandData.getCommand());
                PartialBotApiMethod<? extends Serializable> result = callbackStack.handle(callbackData);
                if (result instanceof SendMessage) {
                    return MessageUtils.sendMessage(callbackData.getMessageData())
                            .text(((SendMessage) result).getText())
                            .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                            .build();
                }
                if (result instanceof EditMessageText) {
                    return MessageUtils.sendMessage(callbackData.getMessageData())
                            .text(((EditMessageText) result).getText())
                            .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                            .build();
                }
                log.error("Failed to parse password - unexpected callback result");
                return MessageUtils.sendMessage(callbackData.getMessageData())
                        .text("Error occurred while handling callback, contact support!")
                        .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                        .build();
            }
            return MessageUtils.sendMessage(callbackData.getMessageData())
                    .text(i18N.get("callback.user.user.failed.validate.password"))
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        });
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(i18N.get("callback.user.user.write.new.password"))
                .replyMarkup(InlineUtils.getDefaultNavigationMarkup(i18N.get("callback.default.navigation.data.back"), InlineUtils.trimLastWord(callbackData.getCallbackData())))
                .build();
    }

    public static PartialBotApiMethod<? extends Serializable> getDefaultSelectPluginMessage(CallbackData callbackData) {
        return getDefaultSelectPluginMessage(callbackData, () -> InlineUtils.trimLastWord(callbackData.getCallbackData()));
    }

    public static EditMessageText getDefaultSelectPluginMessage(CallbackData callbackData, Supplier<String> redoCallbackSupplier) {
        I18N i18N = I18N.from(callbackData.getMessageData());
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(i18N.get("util.callback.select.plugin"))
                .replyMarkup(InlineUtils.getListNavigationMarkup(Arrays.stream(Plugin.values())
                                .collect(Collectors.toMap(Plugin::name, Plugin::name)),
                        (data) -> callbackData.getCallbackData() + " " + data,
                        callbackData.getMessageData().getLocale(),
                        redoCallbackSupplier
                ))
                .build();
    }

    public static PartialBotApiMethod<? extends Serializable> getDefaultSelectBandwidthMessage(CallbackData callbackData, String selectBandwidthMessage, Supplier<String> redoCallbackSupplier) {
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(selectBandwidthMessage)
                .replyMarkup(InlineUtils.getListNavigationMarkup(Arrays.stream(Bandwidth.values())
                                .collect(Collectors.toMap(Bandwidth::name, Bandwidth::name)),
                        (data) -> callbackData.getCallbackData() + " " + data,
                        callbackData.getMessageData().getLocale(),
                        redoCallbackSupplier
                ))
                .build();
    }
}
