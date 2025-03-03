package org.Roclh.testutil.callback;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public class CallbackTestUtil {

    @Nullable
    public static String extractCallbackData(InlineKeyboardMarkup inlineKeyboardMarkup, Predicate<InlineKeyboardButton> filter){
        return inlineKeyboardMarkup
                .getKeyboard()
                .stream()
                .flatMap(Collection::stream)
                .filter(filter)
                .findFirst()
                .map(InlineKeyboardButton::getCallbackData)
                .orElse(null);
    }
}
