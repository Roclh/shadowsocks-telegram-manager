package org.Roclh.mock;

import org.Roclh.data.entities.UserModel;
import org.Roclh.sh.scripts.DisableShadowsocksServerScript;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.Roclh.sh.scripts.EnableV2RayShadowsocksServerScript;
import org.Roclh.sh.scripts.RestartShadowsocksServerScript;
import org.Roclh.sh.scripts.ScreenListScript;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class ShScriptsMocks {

    private static final Set<UserModel> enabledUsers = new CopyOnWriteArraySet<>();


    @Bean
    @Primary
    public EnableV2RayShadowsocksServerScript enableV2RayShadowsocksServerScript() {
        EnableV2RayShadowsocksServerScript enableV2RayShadowsocksServerScript = mock(EnableV2RayShadowsocksServerScript.class);
        Mockito.doNothing().when(enableV2RayShadowsocksServerScript).init();
        Mockito.when(enableV2RayShadowsocksServerScript.execute(Mockito.any(UserModel.class))).then((ans) -> {
            enabledUsers.add(ans.getArgument(0));
            return true;
        });
        Mockito.when(enableV2RayShadowsocksServerScript.execute()).then((ans) -> true);
        return enableV2RayShadowsocksServerScript;
    }

    @Bean
    @Primary
    public EnableDefaultShadowsocksServerScript enableDefaultShadowsocksServerScript(){
        EnableDefaultShadowsocksServerScript enableDefaultShadowsocksServerScript = mock(EnableDefaultShadowsocksServerScript.class);
        Mockito.doNothing().when(enableDefaultShadowsocksServerScript).init();
        Mockito.when(enableDefaultShadowsocksServerScript.execute(Mockito.any(), Mockito.any())).then((ans) -> true);
        Mockito.when(enableDefaultShadowsocksServerScript.execute(Mockito.any(UserModel.class))).then((ans) -> {
            enabledUsers.add(ans.getArgument(0));
            return true;
        });
        Mockito.when(enableDefaultShadowsocksServerScript.execute()).then((ans) -> true);
        return enableDefaultShadowsocksServerScript;
    }

    @Bean
    @Primary
    public RestartShadowsocksServerScript restartShadowsocksServerScript(){
        RestartShadowsocksServerScript restartShadowsocksServerScript = mock(RestartShadowsocksServerScript.class);
        Mockito.doNothing().when(restartShadowsocksServerScript).init();
        Mockito.when(restartShadowsocksServerScript.execute(Mockito.any(), Mockito.anyBoolean())).thenReturn(true);
        Mockito.when(restartShadowsocksServerScript.execute()).thenReturn(true);
        return restartShadowsocksServerScript;
    }

    @Bean
    @Primary
    public DisableShadowsocksServerScript disableShadowsocksServerScript(){
        DisableShadowsocksServerScript disableShadowsocksServerScript = mock(DisableShadowsocksServerScript.class);
        Mockito.doNothing().when(disableShadowsocksServerScript).init();
        Mockito.when(disableShadowsocksServerScript.execute(Mockito.any(UserModel.class))).then((ans) -> {
            enabledUsers.remove(ans.getArgument(0));
            return true;
        });
        Mockito.when(disableShadowsocksServerScript.execute()).thenReturn(true);
        return disableShadowsocksServerScript;
    }

    @Bean
    @Primary
    public ScreenListScript screenListScript(){
        ScreenListScript screenListScript = mock(ScreenListScript.class);
        Mockito.doNothing().when(screenListScript).init();
        Mockito.when(screenListScript.execute()).then((ans) -> enabledUsers.stream()
                .map(user -> user.getUserModel().getTelegramId() + ":" + user.getUserModel().getTelegramName())
                .toList());
        return screenListScript;
    }
}
