package org.Roclh.handlers.commands.user;

import org.Roclh.bot.TelegramBot;
import org.Roclh.data.entities.UserModel;
import org.Roclh.data.enums.Plugin;
import org.Roclh.data.services.UserService;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.CommandData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.mock.ShScriptsMocks;
import org.Roclh.mock.TelegramUserMocks;
import org.Roclh.testutil.UserTestBase;
import org.Roclh.testutil.callback.CallbackTestUtil;
import org.Roclh.utils.callback.CallbackStack;
import org.Roclh.utils.i18n.I18N;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;
import java.util.Objects;

@Import(ShScriptsMocks.class)
public class AddUserCommandTest extends UserTestBase {
    @Autowired
    private TelegramUserMocks tgMocks;
    @Autowired
    @InjectMocks
    private UserService userService;
    @Autowired
    @InjectMocks
    private AddUserCommand addUserCommand;
    @Autowired
    @SpyBean
    private TelegramBot telegramBot;

    private ArgumentCaptor<PartialBotApiMethod<?>> sendMessageArgumentCaptor;

    private MessageData rootMessageData;

    @BeforeEach
    public void init() {
        super.init();
        rootMessageData = MessageData.builder()
                .telegramId(tgMocks.tguroot().getTelegramId())
                .chatId(Objects.requireNonNull(tgMocks.tguroot().getTelegramId()))
                .messageId(123456)
                .telegramName(Objects.requireNonNull(tgMocks.tguroot().getTelegramName()))
                .locale(Locale.forLanguageTag("ru"))
                .build();
        Mockito.doNothing().when(telegramBot).sendMessage(Mockito.any());
        sendMessageArgumentCaptor = ArgumentCaptor.forClass(PartialBotApiMethod.class);
    }

    @Test
    public void whenCorrectCommand_thenUserIsAdded() {
        long port = userService.getAvailablePorts(1).get(0);
        String password = "qwertyui";
        String command = "add " + tgMocks.tgu5().getTelegramId() + " " + port + " " + password;
        addUserCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = addUserCommand.handle(CommandData.builder()
                .messageData(rootMessageData)
                .command(command)
                .build());
        Assertions.assertEquals(I18N.from(rootMessageData).get("command.user.add.success", tgMocks.tgu5().getTelegramId()),
                result.getText());
        Assertions.assertAll(
                () -> Assertions.assertTrue(userService.getUser(tgMocks.tgu5().getTelegramId()).isPresent()),
                () -> Assertions.assertTrue(userService.getUser(tgMocks.tgu5().getTelegramId()).map(
                        user ->
                                user.isEnabled()
                                        && user.getUserModel().getTelegramId().equals(tgMocks.tgu5().getTelegramId())
                                        && Objects.requireNonNull(user.getUsedPort()).equals(port)
                                        && Objects.requireNonNull(user.getPassword()).equals(password)
                                        && user.getPlugin().equals(Plugin.DEFAULT)
                ).orElse(false))
        );
        Mockito.verify(telegramBot, Mockito.atLeast(1)).sendMessage(sendMessageArgumentCaptor.capture());
        Assertions.assertTrue(sendMessageArgumentCaptor.getAllValues().stream().anyMatch(
                (message) -> {
                    if (message instanceof SendMessage sendMessage) {
                        return sendMessage.getText()
                                .equals(I18N
                                        .from(Locale.forLanguageTag("ru"))
                                        .get("command.common.adduserwithoutpassword.granted.access")
                                ) &&
                                Objects.equals(tgMocks.tgu5().getChatId(), Long.valueOf(sendMessage.getChatId()));
                    }
                    return false;
                }
        ));
    }

    @Test
    public void whenIncorrectCommandLength_thenUserIsNotAdded() {
        I18N i18N = I18N.from(rootMessageData.getLocale());
        long port = userService.getAvailablePorts(1).get(0);
        String commandTooLong = "add " + tgMocks.tgu3().getTelegramId() + " " + port + " qwertyui and another words in password";
        addUserCommand.setI18N(rootMessageData.getLocale());
        SendMessage resultTooLong = addUserCommand.handle(CommandData.builder()
                .messageData(rootMessageData)
                .command(commandTooLong)
                .build()
        );
        Assertions.assertEquals(i18N.get("command.user.add.validation.password"), resultTooLong.getText());
        Assertions.assertFalse(userService.isAddedUser(tgMocks.tgu3().getTelegramId()));

        String commandTooShort = "add " + tgMocks.tgu3().getTelegramId();
        SendMessage resultTooShort = addUserCommand.handle(CommandData.builder()
                .messageData(rootMessageData)
                .command(commandTooShort)
                .build());
        Assertions.assertEquals(i18N.get("common.validation.not.enough.argument", 4),
                resultTooShort.getText());
        Assertions.assertFalse(userService.isAddedUser(tgMocks.tgu3().getTelegramId()));
    }

    @Test
    public void whenIncorrectTelegramId_thenFailedToParseTelegramIdMessage() {
        long port = userService.getAvailablePorts(1).get(0);
        String command = "add " + tgMocks.tgu3().getTelegramId() + "faf " + port + " qwertyui";
        addUserCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = addUserCommand.handle(CommandData.builder()
                .messageData(rootMessageData)
                .command(command)
                .build()
        );
        Assertions.assertEquals(
                I18N.from(rootMessageData).get(
                        "command.user.add.validation.parse.telegram.id",
                        tgMocks.tgu3().getTelegramId() + "faf"),
                result.getText());
    }

    @Test
    public void whenIncorrectPort_thenFailedToParsePortMessage() {
        String incorrectPort = "1234asd";
        String command = "add " + tgMocks.tgu3().getTelegramId() + " " + incorrectPort + " qwertyui";
        addUserCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = addUserCommand.handle(CommandData.builder()
                .messageData(rootMessageData)
                .command(command)
                .build());
        Assertions.assertEquals(
                I18N.from(rootMessageData).get("command.user.add.validation.parse.port", incorrectPort),
                result.getText()
        );
    }

    @Test
    public void whenIncorrectPassword_thenFailedToValidatePasswordMessage() {
        String incorrectPassword = "qwery\"'";
        long port = userService.getAvailablePorts(1).get(0);
        String command = "add " + tgMocks.tgu3().getTelegramId() + " " + port + " " + incorrectPassword;
        addUserCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = addUserCommand.handle(CommandData.builder()
                .messageData(rootMessageData)
                .command(command)
                .build());
        Assertions.assertEquals(
                I18N.from(rootMessageData).get("command.user.add.validation.validate.password", incorrectPassword),
                result.getText()
        );
    }

    @Test
    public void whenPortAlreadyInUse_thenPortAlreadyInUseMessage() {
        UserModel existingUser = userService.getActiveUsers().get(0);
        Assertions.assertNotNull(existingUser);
        Assertions.assertNotNull(existingUser.getUsedPort());
        long port = existingUser.getUsedPort();
        String command = "add " + tgMocks.tgu3().getTelegramId() + " " + port + " qwertyui";
        addUserCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = addUserCommand.handle(CommandData.builder()
                .command(command)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(I18N.from(rootMessageData).get("command.user.add.validation.port.already.in.use", port),
                result.getText());
        Assertions.assertFalse(userService.isAddedUser(tgMocks.tgu3().getTelegramId()));
    }

    @Test
    public void whenTelegramUserNotExists_thenTelegramUserNotExistsMessage() {
        long nonExistingId = 155345689L;
        long port = userService.getAvailablePorts(1).get(0);
        String command = "add " + nonExistingId + " " + port + " qwertyui";
        addUserCommand.setI18N(rootMessageData.getLocale());
        SendMessage result = addUserCommand.handle(CommandData.builder()
                .messageData(rootMessageData)
                .command(command)
                .build());
        Assertions.assertEquals(I18N.from(rootMessageData).get("command.user.add.validation.user.dont.exist", nonExistingId),
                result.getText());
        Assertions.assertFalse(userService.isAddedUser(nonExistingId));
    }

    @Test
    public void whenCorrectCallbackStack_thenUserIsAdded() {
        addUserCommand.setI18N(rootMessageData.getLocale());
        CallbackStack callbackStack = addUserCommand.getCallbackStack();
        EditMessageText selectCallbackKeyResult = (EditMessageText) callbackStack.handle(CallbackData
                .builder()
                .callbackData(callbackStack.getCallbackKey())
                .callbackCommand(callbackStack.getCallbackKey())
                .messageData(rootMessageData)
                .build());
        String selectCallbackKeyData = CallbackTestUtil.extractCallbackData(
                selectCallbackKeyResult.getReplyMarkup(),
                (button) -> button.getText()
                        .equals(I18N
                                .from(rootMessageData)
                                .get("callback.user.user.inline.button.add.with.defined.password"))
        );
        Assertions.assertNotNull(selectCallbackKeyData);
        Assertions.assertEquals(selectCallbackKeyData, callbackStack.getCallbackKey() + " add");
        EditMessageText selectCommandResult = (EditMessageText) callbackStack.handle(CallbackData
                .builder()
                .callbackCommand(callbackStack.getCallbackKey())
                .callbackData(selectCallbackKeyData)
                .messageData(rootMessageData)
                .build()
        );
        String selectTgIdResultData = CallbackTestUtil.extractCallbackData(
                selectCommandResult.getReplyMarkup(),
                (button) -> button.getText().equals(
                        tgMocks.tgu3().getTelegramName() + ":" + tgMocks.tgu3().getTelegramId()
                )
        );
        Assertions.assertNotNull(selectTgIdResultData);
        Assertions.assertEquals(selectTgIdResultData, selectCallbackKeyData + " " + tgMocks.tgu3().getTelegramId());
        EditMessageText selectTgIdResult = (EditMessageText) callbackStack.handle(CallbackData
                .builder()
                .callbackCommand(callbackStack.getCallbackKey())
                .callbackData(selectTgIdResultData)
                .messageData(rootMessageData)
                .build());
        long port = userService.getAvailablePorts(5).get(0);
        String selectPortResultData = CallbackTestUtil.extractCallbackData(
                selectTgIdResult.getReplyMarkup(),
                (button) -> button.getText().equals(
                        "Port " + port
                )
        );
        Assertions.assertNotNull(selectPortResultData);
        Assertions.assertEquals(selectPortResultData, selectTgIdResultData + " " + port);
        EditMessageText selectPortResult = (EditMessageText) callbackStack.handle(CallbackData
                .builder()
                .callbackCommand(callbackStack.getCallbackKey())
                .callbackData(selectPortResultData)
                .messageData(rootMessageData)
                .build());
        Assertions.assertEquals(I18N.from(rootMessageData).get("callback.user.user.write.new.password"), selectPortResult.getText());
        Update mockUpdateWithPassword = mockMessageFrom(rootMessageData);
        Mockito.when(mockUpdateWithPassword.getMessage().getText()).thenReturn("qwertyui");
        telegramBot.onUpdateReceived(mockUpdateWithPassword);
        Mockito.verify(telegramBot, Mockito.atLeast(1)).sendMessage(sendMessageArgumentCaptor.capture());
        Assertions.assertTrue(sendMessageArgumentCaptor.getAllValues().stream().anyMatch(message -> {
            if (message instanceof SendMessage sendMessage) {
                return sendMessage.getText().equals(I18N.from(rootMessageData).get("command.user.add.success", tgMocks.tgu3().getTelegramId()))
                        && rootMessageData.getChatId().equals(Long.valueOf(sendMessage.getChatId()));
            }
            return false;
        }));
        Assertions.assertTrue(sendMessageArgumentCaptor.getAllValues().stream().anyMatch(
                (message) -> {
                    if (message instanceof SendMessage sendMessage) {
                        return sendMessage.getText()
                                .equals(I18N
                                        .from(Locale.forLanguageTag("ru"))
                                        .get("command.common.adduserwithoutpassword.granted.access")
                                )
                                && Objects.equals(tgMocks.tgu3().getChatId(), Long.valueOf(sendMessage.getChatId()));
                    }
                    return false;
                }
        ));
        Assertions.assertTrue(userService.isAddedUser(tgMocks.tgu3().getTelegramId()));
    }

    //TODO: Find out how to test script execution with mock scripts
}
