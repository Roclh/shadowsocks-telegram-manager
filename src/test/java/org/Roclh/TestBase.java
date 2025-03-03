package org.Roclh;

import org.Roclh.handlers.messaging.MessageData;
import org.Roclh.sh.scripts.CreateBandwidthRulesetScript;
import org.Roclh.sh.scripts.DisableShadowsocksServerScript;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.Roclh.sh.scripts.EnableV2RayShadowsocksServerScript;
import org.Roclh.sh.scripts.RestartShadowsocksServerScript;
import org.Roclh.sh.scripts.ScreenListScript;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
                CreateBandwidthRulesetScript.class,
                EnableDefaultShadowsocksServerScript.class,
                EnableV2RayShadowsocksServerScript.class,
                RestartShadowsocksServerScript.class,
                DisableShadowsocksServerScript.class,
                ScreenListScript.class
        }
))
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class TestBase {

    protected static PostgreSQLContainer<?> postgres;

    @BeforeAll
    static void startContainer(){
        postgres = new PostgreSQLContainer<>("postgres:16.4")
                .withDatabaseName("pepegavpnmanagertest")
                .withUsername("postgres")
                .withPassword("qwertyui");
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    public void testDatabaseConnection(){
        Assert.isTrue(postgres.isRunning(), "Postgress container should be running");
    }

    public Update mockMessageFrom(MessageData messageData){
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(messageData.getTelegramId());
        when(user.getUserName()).thenReturn(messageData.getTelegramName());
        when(message.getChatId()).thenReturn(messageData.getChatId());
        when(message.getMessageId()).thenReturn(messageData.getMessageId());
        when(message.hasText()).thenReturn(true);
        when(message.getFrom()).thenReturn(user);
        when(update.getMessage()).thenReturn(message);
        when(update.hasMessage()).thenReturn(true);
        return update;
    }
}
