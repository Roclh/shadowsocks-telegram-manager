package org.Roclh.handlers.commands.telegramuser;

import org.Roclh.data.Role;
import org.Roclh.data.services.TelegramUserService;
import org.Roclh.handlers.commands.telegramUser.SetRoleCommand;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.testutil.TelegramUserTestBase;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Locale;
import java.util.Objects;

public class SetRoleCommandTest extends TelegramUserTestBase {

    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private SetRoleCommand setRoleCommand;

    private MessageData rootMessageData;
    private MessageData managerMessageData;

    @BeforeEach
    public void init() {
        super.init();
        rootMessageData = MessageData.builder()
                .telegramId(tgMocks.tguroot().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tguroot().getChatId()))
                .messageId(123456)
                .telegramName(Objects.requireNonNull(tgMocks.tguroot().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
        managerMessageData = MessageData.builder()
                .telegramId(tgMocks.tgu4().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tgu4().getChatId()))
                .messageId(123456)
                .telegramName(Objects.requireNonNull(tgMocks.tgu4().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
    }

    @Test
    public void whenCorrectCommand_thenRoleChanged() {
        String command = "role " + tgMocks.tgu2().getTelegramId() + " " + Role.USER;
        setRoleCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = setRoleCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(I18N.from(rootMessageData).get("command.manager.setrole.success", tgMocks.tgu2().getTelegramId()), result.getText());
        Assertions.assertAll(
                () -> Assertions.assertTrue(telegramUserService.getUser(tgMocks.tgu2().getTelegramId()).isPresent()),
                () -> Assertions.assertTrue(telegramUserService.getUser(tgMocks.tgu2().getTelegramId())
                        .map(user -> user.getRole().equals(Role.USER)).orElse(false))
        );
    }

    @Test
    public void whenNotEnoughRights_thenRoleIsNotChanged() {
        String command = "role " + tgMocks.tgu2().getTelegramId() + " " + Role.ROOT;
        setRoleCommand.setI18N(managerMessageData.getLocale());
        SendMessage result = setRoleCommand.handle(CommandData.builder()
                .command(command)
                .messageData(managerMessageData)
                .build());
        Assertions.assertEquals(I18N.from(managerMessageData).get("command.manager.setrole.validation.is.not.allowed"), result.getText());
        Assertions.assertAll(
                () -> Assertions.assertTrue(telegramUserService.getUser(tgMocks.tgu2().getTelegramId()).isPresent()),
                () -> Assertions.assertTrue(telegramUserService.getUser(tgMocks.tgu2().getTelegramId())
                        .map(user -> user.getRole().equals(tgMocks.tgu2().getRole())).orElse(false))
        );
    }

    @Test
    public void whenWrongNumber_thenNumberFormatErrorText() {
        String command = "role " + tgMocks.tgu2().getTelegramId() + "asd " + Role.USER;
        setRoleCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = setRoleCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(I18N.from(rootMessageData).get("command.manager.setrole.validation.wrong.telegram.id",
                        tgMocks.tgu2().getTelegramId() + "asd"),
                result.getText());
        Assertions.assertAll(
                () -> Assertions.assertTrue(telegramUserService.getUser(tgMocks.tgu2().getTelegramId()).isPresent()),
                () -> Assertions.assertTrue(telegramUserService.getUser(tgMocks.tgu2().getTelegramId())
                        .map(user -> user.getRole().equals(tgMocks.tgu2().getRole())).orElse(false))
        );
    }

    @Test
    public void whenTelegramUserNotExists_thenNotExistsMessage() {
        long nonExistingId = 12345678L;
        String command = "role " + nonExistingId + " " + Role.USER;
        setRoleCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = setRoleCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(I18N.from(rootMessageData).get("command.manager.setrole.validation.id.not.exists", nonExistingId),
                result.getText());
        Assertions.assertFalse(telegramUserService.getUser(nonExistingId).isPresent());
    }

    @Test
    public void whenNonExistingRole_thenUnkownRoleMessage(){
        String command = "role " + tgMocks.tgu2().getTelegramId() + " ADMIN";
        setRoleCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = setRoleCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(I18N.from(rootMessageData).get("command.manager.setrole.validation.wrong.role", "ADMIN"),
                result.getText());
        Assertions.assertAll(
                () -> telegramUserService.getUser(tgMocks.tgu2().getTelegramId()).isPresent(),
                () -> telegramUserService.getUser(tgMocks.tgu2().getTelegramId())
                        .map(user -> user.getRole().equals(tgMocks.tgu2().getRole())).orElse(false)
        );
    }
}
