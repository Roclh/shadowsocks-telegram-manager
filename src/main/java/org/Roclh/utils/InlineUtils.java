package org.Roclh.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.utils.i18n.EmojiConstants;
import org.Roclh.utils.i18n.I18N;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class InlineUtils {

    private final static Pattern paginationPattern = Pattern.compile("^*\\{[0-9]+}$");

    @NonNull
    public static InlineKeyboardMarkup getListNavigationMarkup(Map<String, String> selectables,
                                                               Function<String, String> callbackDataConsumer,
                                                               Locale locale) {
        return getListNavigationMarkup(selectables, callbackDataConsumer, locale, () -> null);
    }

    public static InlineKeyboardMarkup getListNavigationMarkup(CallbackData callbackData,
                                                               long pageCount) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboardButtons = new ArrayList<>();

        List<InlineKeyboardButton> navigationRows = getPaginationButtonsRow(callbackData, pageCount);
        if (navigationRows != null) {
            inlineKeyboardButtons.add(navigationRows);
        }
        inlineKeyboardMarkup.setKeyboard(inlineKeyboardButtons);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getListNavigationMarkupWithPagination(Map<String, String> selectables,
                                                                             Function<String, String> callbackDataConsumer,
                                                                             CallbackData callbackData,
                                                                             Supplier<String> redoCallbackSupplier,
                                                                             int pageSize) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboardButtons = new ArrayList<>();
        int pageCount;
        if (selectables.size() / pageSize > 0) {
            pageCount = selectables.size() / pageSize + (selectables.size() % pageSize > 0 ? 1 : 0);
        } else {
            pageCount = 0;
        }
        int pageNumber = getPageNumber(callbackData.getCallbackData());
        Assert.isTrue(pageNumber >= 0, "Page number should be bigger or equals than 0");
        int startOfPage = pageSize * pageNumber;
        int endOfPage = pageSize * (pageNumber + 1);
        List<Map.Entry<String, String>> selectableSubList = selectables.entrySet().stream().toList()
                .subList(startOfPage, Math.min(endOfPage, selectables.size()));
        for (Map.Entry<String, String> selectable : selectableSubList) {
            InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton.builder()
                    .text(selectable.getKey())
                    .callbackData(callbackDataConsumer.apply(selectable.getValue()))
                    .build();
            inlineKeyboardButtons.add(List.of(inlineKeyboardButton));
        }
        List<InlineKeyboardButton> navigationRows = getPaginationButtonsRow(callbackData, pageCount);
        if (navigationRows != null) {
            inlineKeyboardButtons.add(navigationRows);
        }
        List<InlineKeyboardButton> backRow = getDefaultRedoLastChangeCommandRow(
                I18N.from(callbackData.getMessageData().getLocale()).get("callback.default.navigation.data.back"),
                redoCallbackSupplier);
        if (backRow != null) {
            inlineKeyboardButtons.add(backRow);
        }
        inlineKeyboardMarkup.setKeyboard(inlineKeyboardButtons);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup combineKeyboardMarkups(InlineKeyboardMarkup... keyboardMarkups) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (InlineKeyboardMarkup markup : keyboardMarkups) {
            rows.addAll(markup.getKeyboard().stream().filter(buttons -> !buttons.isEmpty()).toList());
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    @NonNull
    public static InlineKeyboardMarkup getListNavigationMarkup(Map<String, String> selectables,
                                                               Function<String, String> callbackDataConsumer,
                                                               Locale locale,
                                                               Supplier<String> redoCallbackSupplier) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder inlineKeyboardMarkupBuilder = InlineKeyboardMarkup.builder();

        List<List<InlineKeyboardButton>> inlineKeyboardButtons = new ArrayList<>();
        for (Map.Entry<String, String> selectable : selectables.entrySet()) {
            InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton.builder()
                    .text(selectable.getKey())
                    .callbackData(callbackDataConsumer.apply(selectable.getValue()))
                    .build();
            inlineKeyboardButtons.add(List.of(inlineKeyboardButton));
        }
        List<InlineKeyboardButton> lastRow = getDefaultRedoLastChangeCommandRow(I18N.from(locale)
                .get("callback.default.navigation.data.back"), redoCallbackSupplier);
        if (lastRow != null) {
            inlineKeyboardButtons.add(lastRow);
        }
        return inlineKeyboardMarkupBuilder.keyboard(inlineKeyboardButtons).build();
    }

    /**
     * Returns a default not localized inline keyboard markup
     *
     * @param text         text for inline button
     * @param callbackData callback data for button
     * @return inline keyboard markup with 1 button
     */
    public static InlineKeyboardMarkup getDefaultNavigationMarkup(@NonNull String text, @NonNull String callbackData) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        InlineKeyboardButton backToStartButton = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
        inlineKeyboardButtons.add(backToStartButton);
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtons));
        return inlineKeyboardMarkup;
    }

    /**
     * Returns a default localized inline keyboard markup
     * Passes callback data to inline button callback data
     *
     * @param callbackData callback data to get I18N and callback data
     * @return keyboard markup with 1 button to navigate
     */
    public static InlineKeyboardMarkup getDefaultNavigationMarkup(@NonNull CallbackData callbackData) {
        I18N i18N = I18N.from(callbackData.getMessageData().getLocale());
        return getDefaultNavigationMarkup(EmojiConstants.HOUSE + i18N.get("callback.default.navigation.data.back"), callbackData.getCallbackData());
    }

    /**
     * Returns a default localized inline keyboard markup
     * Callback data that passed to keyboard leads to start, so use it as return button
     *
     * @param messageData message data to get I18N
     * @return keyboard markup with 1 button to navigate to start
     */
    public static InlineKeyboardMarkup getNavigationToStart(@NonNull MessageData messageData) {
        I18N i18N = I18N.from(messageData.getLocale());
        return getDefaultNavigationMarkup(EmojiConstants.HOUSE + i18N.get("callback.default.navigation.data.back"), "start");
    }

    public static InlineKeyboardMarkup getNavigationToPreviousCommand(@NonNull CallbackData callbackData) {
        I18N i18N = I18N.from(callbackData.getMessageData().getLocale());
        return getDefaultNavigationMarkup(i18N.get("callback.default.navigation.data.back"),
                callbackData.getCallbackData().substring(0, callbackData.getCallbackData().lastIndexOf(" ")));
    }

    /**
     * Returns a default row with not localized button for inline keyboard
     * Usually used to trim callback with command, trimmed in data supplier
     *
     * @param redoButtonText       text for inline button
     * @param callbackDataSupplier data supplier for callback
     * @return 1 keyboard row with 1 button
     */
    @Nullable
    public static List<InlineKeyboardButton> getDefaultRedoLastChangeCommandRow(String redoButtonText, Supplier<String> callbackDataSupplier) {
        if (callbackDataSupplier.get() == null) {
            return null;
        }
        InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton.builder()
                .text(redoButtonText)
                .callbackData(callbackDataSupplier.get())
                .build();
        return List.of(inlineKeyboardButton);
    }

    private static List<InlineKeyboardButton> getPaginationButtonsRow(CallbackData callbackData, long pageCount) {
        if (pageCount == 0) {
            return null;
        }
        Assert.isTrue(paginationPattern.matcher(callbackData.getCallbackData()).find(), "Callback data does not contains {[0-9]} at the end of string");
        int pageNumber = getPageNumber(callbackData.getCallbackData());
        Assert.isTrue(pageNumber >= 0 && pageNumber <= pageCount, "Page number expected to be in range from 0 to pageCount");
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (pageNumber > 0) {
            buttons.add(InlineKeyboardButton.builder()
                    .text("<-")
                    .callbackData(replacePage(callbackData, "{" + (pageNumber - 1) + "}"))
                    .build());
        }
        if (pageNumber < pageCount - 1) {
            buttons.add(InlineKeyboardButton.builder()
                    .text("->")
                    .callbackData(replacePage(callbackData, "{" + (pageNumber + 1) + "}"))
                    .build());
        }
        return buttons;
    }

    public static String replacePage(CallbackData callbackData, String arg) {
        return paginationPattern.matcher(callbackData.getCallbackData()).replaceAll(arg);
    }

    public static boolean paginationMatches(String command) {
        boolean hasPagination = paginationPattern.matcher(command).find();
        log.debug("Command {} has pagination? {}", command, hasPagination);
        return hasPagination;
    }

    public static int getPageNumber(String data) {
        try {
            Matcher matcher = paginationPattern.matcher(data);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group().replace("{", "").replace("}", ""));
            } else {
                return -1;
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse data", e);
            return -1;
        } catch (IllegalStateException e) {
            log.error("Failed to parse data, data {} does not match pattern {}", data, paginationPattern.pattern());
            return -1;
        }
    }
}
