package org.Roclh.utils.callback;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.utils.InlineUtils;
import org.Roclh.utils.MessageUtils;
import org.Roclh.utils.i18n.I18N;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Roclh
 * @since 22.02.2025
 * <p>
 * Class to simplify building callback handlers
 * <p>
 * To build new callbackStack you need to refer to callbackStackBuilder by static method .of(), where you pass callbackKey
 * Callback key is required to combine related callbacks into one callbackStack to reduce duplicated values stored in memory
 * <p>
 * After method ,of() method .forCommand() is required, since building callbackStack without command is incorrect
 * .forCommand() method gets a String with command to add into callback stack
 * <p>
 * To add a handler for exact amount of parameters for command use method .with() that gets amount of arguments expected in handler
 * and a CallbackArgumentHandler function that defines how callbackData will be processed
 * <p>
 * After all required args are passed, use .build() to get a CallbackStack from CallbackStackBuilder
 * <p>
 * CallbackStacks can be merged with method .merge(). You can merge only callbackStacks with same callbackKey. All callbacks for each
 * command are preserved in callbackStack map
 */

@Slf4j
@Getter
public class CallbackStack {
    private final String callbackKey;
    private String localizedCallbackKey;
    private String selectCommandText;
    private final boolean util;
    private final Map<String, String> commandLocalizationMap;
    private final Map<String, Predicate<Long>> commandDisplayConditionsMap;
    private final Map<String, Map<Integer, CallbackArgumentHandler>> callbackStack;

    private CallbackStack(String callbackKey,
                          String localizedCallbackKey,
                          String selectCommandText,
                          boolean util,
                          Map<String, String> commandLocalizationMap,
                          Map<String, Predicate<Long>> commandDisplayConditionsMap,
                          Map<String, Map<Integer, CallbackArgumentHandler>> callbackStack) {
        this.callbackKey = callbackKey;
        this.localizedCallbackKey = localizedCallbackKey;
        this.selectCommandText = selectCommandText;
        this.util = util;
        this.commandLocalizationMap = commandLocalizationMap;
        this.commandDisplayConditionsMap = commandDisplayConditionsMap;
        this.callbackStack = callbackStack;
    }

    public CallbackStack merge(@NonNull CallbackStack callbackStack) {
        Assert.isTrue(this.callbackKey.equals(callbackStack.getCallbackKey()), "Callback stacks can be merged only if contains same key");
        this.callbackStack.putAll(callbackStack.getCallbackStack());
        this.commandLocalizationMap.putAll(callbackStack.getCommandLocalizationMap());
        this.commandDisplayConditionsMap.putAll(callbackStack.getCommandDisplayConditionsMap());
        this.localizedCallbackKey = callbackKey.equals(localizedCallbackKey) ? callbackStack.getLocalizedCallbackKey() : localizedCallbackKey;
        this.selectCommandText = this.selectCommandText == null ? callbackStack.getSelectCommandText() : this.selectCommandText;
        return this;
    }

    @NonNull
    public static CallbackStackBuilder of(@NonNull String callbackKey) {
        return new CallbackStackBuilder(callbackKey);
    }

    public PartialBotApiMethod<? extends Serializable> handle(@NonNull CallbackData callbackData) {
        Assert.isTrue(this.callbackKey.equals(callbackData.getCallbackCommand()), "Wrong callback handler!");
        String[] words = callbackData.getCallbackData().split(" ");
        I18N i18N = I18N.from(callbackData.getMessageData().getLocale());
        int len = words.length - 1 - (InlineUtils.paginationMatches(callbackData.getCallbackData()) ? 1 : 0);
        if (callbackStack.containsKey(words[0])) {
            Map<Integer, CallbackArgumentHandler> callbackArgHandler = callbackStack.get(words[0]);
            if (callbackArgHandler.containsKey(len)) {
                return callbackArgHandler.get(len).handle(callbackData);
            }
            log.error("Failed to handle callbackStack {} with command {}: callbackArgHandler does not contain key {}", callbackKey, words[0], len);
            return MessageUtils.sendMessage(callbackData.getMessageData())
                    .text("Error occurred while handling callback, contact support!")
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        }
        if (len == 0) {
            return MessageUtils.editMessage(callbackData.getMessageData())
                    .text(selectCommandText == null ? i18N.get("callback.common.select.command") : selectCommandText)
                    .replyMarkup(InlineUtils.getListNavigationMarkup(commandLocalizationMap.entrySet()
                                    .stream()
                                    .filter(localizationEntry -> {
                                        if (!commandDisplayConditionsMap.containsKey(localizationEntry.getValue())) {
                                            return true;
                                        }
                                        return commandDisplayConditionsMap.get(localizationEntry.getValue()).test(callbackData.getMessageData().getTelegramId());
                                    })
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                            (data) -> callbackData.getCallbackData() + " " + data,
                            callbackData.getMessageData().getLocale(),
                            () -> "start"))
                    .build();
        }
        String command = words[1];
        if (callbackStack.containsKey(command)) {
            Map<Integer, CallbackArgumentHandler> callbackArgHandler = callbackStack.get(command);
            if (callbackArgHandler.containsKey(len)) {
                return callbackArgHandler.get(len).handle(callbackData);
            }
            log.error("Failed to handle callbackStack {} with command {}: callbackArgHandler does not contain key {}", callbackKey, command, len);
            return MessageUtils.sendMessage(callbackData.getMessageData())
                    .text("Error occurred while handling callback, contact support!")
                    .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                    .build();
        }
        log.error("Failed to handle callbackStack {} with command {}: callbackStack does not contain handler for command {}", callbackKey, command, command);
        return MessageUtils.sendMessage(callbackData.getMessageData())
                .text("Error occurred while handling callback, contact support!")
                .replyMarkup(InlineUtils.getNavigationToStart(callbackData.getMessageData()))
                .build();
    }

    public boolean refers(CallbackData callbackData) {
        String[] words = callbackData.getCallbackData().split(" ");
        if (words.length <= 1) {
            return this.getCallbackKey().equals(callbackData.getCallbackCommand());
        }
        return this.getCallbackKey().equals(callbackData.getCallbackCommand()) &&
                (this.getCallbackKey().equals(words[0]) || this.getCallbackStack().containsKey(words[1]));
    }

    public List<InlineKeyboardButton> getCallbackStackButton() {
        return List.of(
                InlineKeyboardButton.builder()
                        .text(this.getLocalizedCallbackKey())
                        .callbackData(this.getCallbackKey())
                        .build()
        );
    }

    public List<InlineKeyboardButton> getCallbackStackButton(Long tgId) {
        return commandDisplayConditionsMap.values()
                        .stream()
                        .anyMatch(condition -> condition.test(tgId)) ?
                List.of(
                        InlineKeyboardButton.builder()
                                .text(this.getLocalizedCallbackKey())
                                .callbackData(this.getCallbackKey())
                                .build()
                ) : null;
    }

    public static class CallbackStackBuilder {
        private final String callbackKey;
        private String localizedCallbackKey;
        private String selectCommandText = null;
        private String command = null;
        private boolean util = false;
        private final Map<String, Predicate<Long>> displayConditions = new ConcurrentHashMap<>();
        private final Map<String, String> commandLocalizationMap = new LinkedHashMap<>();
        private final Map<String, Map<Integer, CallbackArgumentHandler>> callbackStack = new LinkedHashMap<>();

        private CallbackStackBuilder(String callbackKey) {
            this.callbackKey = callbackKey;
            this.localizedCallbackKey = callbackKey;
        }

        public CallbackStackBuilder withLocalizedCallbackKey(String callbackKeyLocalization) {
            this.localizedCallbackKey = callbackKeyLocalization;
            return this;
        }

        public CallbackStackBuilder withCommandDisplayCondition(Predicate<Long> displayCondition) {
            Assert.notNull(command, "Condition requires a command");
            this.displayConditions.put(command, displayCondition);
            return this;
        }

        public CallbackStackBuilder forCommand(String command) {
            this.command = command;
            this.displayConditions.put(command, (tgId) -> true);
            return this;
        }

        public CallbackStackBuilder withSelectCommandText(String selectCommandText) {
            this.selectCommandText = selectCommandText;
            return this;
        }

        public CallbackStackBuilder forCommand(String command, Map<String, String> commandLocalizationMap) {
            this.command = command;
            this.commandLocalizationMap.putAll(commandLocalizationMap);
            this.displayConditions.put(command, (tgId) -> true);
            return this;
        }

        public CallbackStackBuilder forCommand(String command, String localizedCommand) {
            this.command = command;
            this.commandLocalizationMap.put(localizedCommand, command);
            this.displayConditions.put(command, (tgId) -> true);
            return this;
        }

        public CallbackStackBuilder with(int commandSize, @NonNull CallbackArgumentHandler callbackArgumentHandler) {
            Assert.notNull(command, "Command for callback stack is not defined");
            if (callbackStack.containsKey(command)) {
                callbackStack.get(command).put(commandSize, callbackArgumentHandler);
            } else {
                callbackStack.put(command, new ConcurrentHashMap<>(Map.of(commandSize, callbackArgumentHandler)));
            }
            return this;
        }

        public CallbackStackBuilder util(boolean isUtil) {
            this.util = isUtil;
            return this;
        }

        public CallbackStack build() {
            return new CallbackStack(callbackKey,
                    localizedCallbackKey,
                    selectCommandText,
                    util,
                    commandLocalizationMap,
                    displayConditions,
                    callbackStack);
        }

    }

    public interface CallbackArgumentHandler {
        PartialBotApiMethod<? extends Serializable> handle(CallbackData callbackData);
    }
}
