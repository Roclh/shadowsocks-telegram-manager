package org.Roclh.handlers;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.data.services.LocalizationService;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.callbacks.common.DefaultCallback;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.handlers.registry.CommandRegistry;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class CallbackHandler {
    private final LocalizationService localizationService;
    private final CommandRegistry commandRegistry;
    private final DefaultCallback defaultCallback;

    public CallbackHandler(CommandRegistry commandRegistry,
                           DefaultCallback defaultCallback,
                           LocalizationService localizationService) {
        this.localizationService = localizationService;
        this.commandRegistry = commandRegistry;
        this.defaultCallback = defaultCallback;
    }

    public PartialBotApiMethod<? extends Serializable> handleCallbacks(Update update) {
        Locale locale = localizationService.getOrCreate(update.getCallbackQuery().getFrom().getId());
        List<CallbackStack> callbackStacks = commandRegistry.getMergedRegisteredCallbacks(update.getCallbackQuery().getFrom().getId(), locale);
        return handleCallbacks(CallbackData.from(update.getCallbackQuery(), locale), callbackStacks);
    }

    public PartialBotApiMethod<? extends Serializable> handleCallbacks(CallbackData callbackData, List<CallbackStack> callbackStacks) {
        MessageData messageData = callbackData.getMessageData();
        long chatId = messageData.getChatId();
        log.info("Received a callback from id {} with data {}", chatId, callbackData);
        log.info("Existing keys: {}", callbackStacks.stream().map(CallbackStack::getCallbackKey).toList());

        if (callbackStacks.stream().anyMatch(callbackStack -> callbackStack.refers(callbackData))) {
            return callbackStacks.stream().filter(callbackStack -> callbackStack.refers(callbackData)).findFirst()
                    .map(callbackStack -> callbackStack.handle(callbackData)).orElse(null);
        }
        return defaultCallback.setI18N(messageData.getLocale()).apply(callbackData);

    }
}
