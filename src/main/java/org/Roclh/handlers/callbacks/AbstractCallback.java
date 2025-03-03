package org.Roclh.handlers.callbacks;

import org.Roclh.data.enums.Role;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.i18n.I18N;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class AbstractCallback<T extends PartialBotApiMethod<? extends Serializable>> implements Callback<T>{
    protected I18N i18N;

    @Override
    public Callback<T> setI18N(Locale locale) {
        i18N = I18N.from(locale);
        return this;
    }

    protected InlineKeyboardMarkup getSelectRoleMarkup(CallbackData callbackData){
        return InlineUtils.getListNavigationMarkup(
                Arrays.stream(Role.values())
                        .collect(Collectors.toMap(Role::name, Role::name)),
                (role) -> callbackData.getCallbackData() + " " + role,
                callbackData.getMessageData().getLocale(),
                () -> trimLastWord(callbackData.getCallbackData())
        );
    }

    protected InlineKeyboardMarkup getSelectLangMarkup(CallbackData callbackData, List<String> supportedLocales) {
        return InlineUtils.getListNavigationMarkup(supportedLocales
                        .stream().collect(Collectors.toMap(lang -> i18N.get(lang), lang -> lang)),
                (data) -> callbackData.getCallbackData() + " " + data,
                callbackData.getMessageData().getLocale(),
                () -> "start"
        );
    }

    protected String trimLastWord(String data) {
        return data.substring(0, data.lastIndexOf(" "));
    }
}
