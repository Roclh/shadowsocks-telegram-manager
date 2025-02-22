package org.Roclh.util.callback;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.TestBase;
import org.Roclh.handlers.messaging.CallbackData;
import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.utils.callback.CallbackStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Locale;

@Slf4j
public class CallbackStackTest extends TestBase {

    @Test
    public void whenCSWithoutCommand_thenIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                CallbackStack.of("test")
                        .with(1, (callbackData) -> {
                            log.info("Some callback handling");
                            return null;
                        })
                        .build());

    }

    @Test
    public void whenCsWithDifferentCallbackKeysMerged_thenIllegalArgument() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                CallbackStack.of("test1")
                        .forCommand("test12", "localizedTest12")
                        .with(1, (callbackData) -> {
                            log.info("Some callback handling 1");
                            return null;
                        })
                        .build().merge(
                                CallbackStack.of("test2")
                                        .forCommand("test123", "localizedTest123")
                                        .with(1, (callbackData) -> {
                                            log.info("Some callback handling 2");
                                            return null;
                                        })
                                        .build()

                        ));
    }

    @Test
    public void whenCorrectCsMerged_thenValid() {
        String callbackKey = "test";
        String command1 = "testCommand1";
        String command2 = "testCommand2";
        CallbackStack first = CallbackStack.of(callbackKey)
                .forCommand(command1)
                .with(1, (callbackData) -> {
                    log.info("Test command 1 callback");
                    Assertions.assertEquals(callbackKey, callbackData.getCallbackCommand());
                    Assertions.assertEquals(command1, callbackData.getCallbackData().split(" ")[1]);
                    return null;
                })
                .build();
        CallbackStack second = CallbackStack.of(callbackKey)
                .forCommand(command2)
                .with(1, (callbackData) -> {
                    log.info("Test command 2 callback");
                    Assertions.fail("This callback stack should not be called");
                    return null;
                })
                .build();
        CallbackStack merged = first.merge(second);
        Assertions.assertNotNull(merged);
        Assertions.assertEquals(first.getCallbackKey(), merged.getCallbackKey());
        Assertions.assertEquals(second.getCallbackKey(), merged.getCallbackKey());
        Assertions.assertEquals(first.getCallbackStack().get(command1), merged.getCallbackStack().get(command1));
        Assertions.assertEquals(second.getCallbackStack().get(command2), merged.getCallbackStack().get(command2));
        Assertions.assertEquals(2, merged.getCallbackStack().size());
        merged.handle(CallbackData.builder()
                .callbackCommand(callbackKey)
                .callbackData(callbackKey + " " + command1)
                .messageData(MessageData.builder()
                        .chatId(1L)
                        .locale(Locale.ENGLISH)
                        .telegramId(1L)
                        .telegramName("Test name")
                        .messageId(1)
                        .build())
                .build());
        Assertions.assertThrows(AssertionFailedError.class, () ->
                merged.handle(CallbackData.builder()
                        .callbackCommand(callbackKey)
                        .callbackData(callbackKey + " " + command2)
                        .messageData(MessageData.builder()
                                .chatId(1L)
                                .locale(Locale.ENGLISH)
                                .telegramId(1L)
                                .telegramName("Test name")
                                .messageId(1)
                                .build())
                        .build()));
    }


}
