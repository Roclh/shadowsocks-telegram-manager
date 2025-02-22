package org.Roclh.utils.callback;

import org.Roclh.data.Role;
import org.Roclh.data.entities.TelegramUserModel;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.i18n.I18N;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CallbackStackUtils {

    public static EditMessageText getDefaultSelectPortMessage(CallbackData callbackData, String portMessage, UserService userService){
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

    public static EditMessageText getDefaultSelectTelegramUserIdMessage(CallbackData callbackData, String userMessage, TelegramUserService telegramUserService, Predicate<TelegramUserModel> filter){
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(userMessage)
                .replyMarkup(InlineUtils.getListNavigationMarkup(telegramUserService
                                .getUsers()
                                .stream()
                                .filter(filter)
                                .collect(Collectors.toMap(user -> user.getTelegramName() + ":" + user.getTelegramId(),
                                        user -> user.getTelegramId().toString())),
                        (data) -> callbackData.getCallbackData() + " " + data,
                        callbackData.getMessageData().getLocale(),
                        () -> InlineUtils.trimLastWord(callbackData.getCallbackData())
                ))
                .build();
    }

    public static EditMessageText getDefaultSelectUserIdMessage(CallbackData callbackData, String userMessage, UserService userService, Predicate<UserModel> filter){
        return MessageUtils.editMessage(callbackData.getMessageData())
                .text(userMessage)
                .replyMarkup(InlineUtils.getListNavigationMarkup(userService
                                .getAllUsers()
                                .stream()
                                .filter(filter)
                                .collect(Collectors.toMap(user -> user.getUserModel().getTelegramName() + ":" + user.getUserModel().getTelegramId(),
                                        user -> user.getUserModel().getTelegramId().toString())),
                        (data) -> callbackData.getCallbackData() + " " + data,
                        callbackData.getMessageData().getLocale(),
                        () -> InlineUtils.trimLastWord(callbackData.getCallbackData())
                ))
                .build();
    }

    public static EditMessageText getDefaultSelectRoleMessage(CallbackData callbackData, Role selectedRole, String userMesage, TelegramUserService telegramUserService){
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

    public static EditMessageText getDefaultSelectLangMessage(CallbackData callbackData, List<String> supportedLocales){
        I18N i18N = I18N.from(callbackData.getMessageData().getLocale());
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
}
