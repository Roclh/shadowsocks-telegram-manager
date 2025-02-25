package org.Roclh.mock;

import org.Roclh.sh.scripts.CreateBandwidthRulesetScript;
import org.Roclh.sh.scripts.EnableDefaultShadowsocksServerScript;
import org.Roclh.sh.scripts.EnableV2RayShadowsocksServerScript;
import org.Roclh.sh.scripts.RestartShadowsocksServerScript;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class ShScriptsMocks {

    @Bean
    @Primary
    public CreateBandwidthRulesetScript createBandwidthRulesetScript() {
        CreateBandwidthRulesetScript createBandwidthRulesetScript = mock(CreateBandwidthRulesetScript.class);
        Mockito.doNothing().when(createBandwidthRulesetScript).init();
        Mockito.when(createBandwidthRulesetScript.execute(Mockito.any())).then((ans) -> false);
        Mockito.when(createBandwidthRulesetScript.execute()).then((ans) -> false);
        return createBandwidthRulesetScript;
    }

    @Bean
    @Primary
    public EnableV2RayShadowsocksServerScript enableV2RayShadowsocksServerScript() {
        EnableV2RayShadowsocksServerScript enableV2RayShadowsocksServerScript = mock(EnableV2RayShadowsocksServerScript.class);
        Mockito.doNothing().when(enableV2RayShadowsocksServerScript).init();
        Mockito.when(enableV2RayShadowsocksServerScript.execute(Mockito.any(), Mockito.any())).then((ans) -> true);
        Mockito.when(enableV2RayShadowsocksServerScript.execute()).then((ans) -> true);
        return enableV2RayShadowsocksServerScript;
    }

    @Bean
    @Primary
    public EnableDefaultShadowsocksServerScript enableDefaultShadowsocksServerScript(){
        EnableDefaultShadowsocksServerScript enableDefaultShadowsocksServerScript = mock(EnableDefaultShadowsocksServerScript.class);
        Mockito.doNothing().when(enableDefaultShadowsocksServerScript).init();
        Mockito.when(enableDefaultShadowsocksServerScript.execute(Mockito.any(), Mockito.any())).then((ans) -> true);
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

}
