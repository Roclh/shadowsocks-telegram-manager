package org.Roclh.handlers.registry;

import org.Roclh.handlers.commands.Command;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.testutil.TelegramUserTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CommandRegistryTest extends TelegramUserTestBase {
    @Autowired
    private CommandRegistry commandRegistry;
    @Autowired
    private TelegramUserMocks tgMocks;

    @Test
    public void whenGetRegisteredCommands_thenExcludeRestricted(){
        List<? extends Command<?>> commandsForGuest = commandRegistry.getRegisteredCommands(tgMocks.tgu2().getTelegramId());
        List<? extends Command<?>> commandsForUser =  commandRegistry.getRegisteredCommands(tgMocks.tgu1().getTelegramId());
        List<? extends Command<?>> commandsForManager = commandRegistry.getRegisteredCommands(tgMocks.tgu4().getTelegramId());
        List<? extends Command<?>> commandsForRoot = commandRegistry.getRegisteredCommands(tgMocks.tguroot().getTelegramId());
        Assertions.assertAll(
                () -> Assertions.assertTrue(commandsForGuest.size() < commandsForUser.size()),
                () -> Assertions.assertTrue(commandsForUser.size() < commandsForManager.size()),
                () -> Assertions.assertTrue(commandsForManager.size() < commandsForRoot.size())
        );
        Assertions.assertAll(
                () -> Assertions.assertTrue(commandsForUser.containsAll(commandsForGuest)),
                () -> Assertions.assertFalse(commandsForGuest.containsAll(commandsForUser)),
                () -> Assertions.assertTrue(commandsForManager.containsAll(commandsForUser)),
                () -> Assertions.assertFalse(commandsForUser.containsAll(commandsForManager)),
                () -> Assertions.assertTrue(commandsForRoot.containsAll(commandsForManager)),
                () -> Assertions.assertFalse(commandsForManager.containsAll(commandsForRoot))
        );
    }
}
