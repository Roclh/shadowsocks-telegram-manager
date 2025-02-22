package org.Roclh.handlers.registry;

import lombok.NonNull;
import org.Roclh.handlers.commands.Command;
import org.Roclh.handlers.commands.WithCallbackStack;
import org.Roclh.utils.callback.CallbackStack;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CommandRegistry {
    private final Map<List<String>, Command<? extends PartialBotApiMethod<?>>> registredCommands = new ConcurrentHashMap<>();

    public void register(@NonNull Command<?> command) {
        registredCommands.put(command.getCommandNames(), command);
    }

    public List<Command<?>> getAllRegisteredCommands() {
        return registredCommands.values().stream().toList();
    }

    public List<? extends Command<? extends PartialBotApiMethod<?>>> getRegisteredCommands(Long userId) {
        return registredCommands.values()
                .stream()
                .filter(command -> command.isAllowed(userId))
                .toList();
    }

    public List<? extends Command<? extends PartialBotApiMethod<?>>> getRegisteredCommands(Long userId, Locale locale) {
        return registredCommands.values()
                .stream()
                .filter(command -> command.isAllowed(userId))
                .peek(command -> command.setI18N(locale))
                .toList();
    }

    public List<CallbackStack> getMergedRegisteredCallbacks(Long userId, Locale locale) {
        return registredCommands.values()
                .stream()
                .filter(command -> command.isAllowed(userId) && command instanceof WithCallbackStack)
                .peek(command -> command.setI18N(locale))
                .map(command -> ((WithCallbackStack) command).getCallbackStack())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(CallbackStack::getCallbackKey))
                .values()
                .stream()
                .map(group -> group.stream().reduce(CallbackStack::merge)
                        .orElseThrow())
                .sorted(Comparator.comparing(CallbackStack::getCallbackKey))
                .toList();
    }
}
