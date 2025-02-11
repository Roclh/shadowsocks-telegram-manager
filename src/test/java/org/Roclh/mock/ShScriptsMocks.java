package org.Roclh.mock;

import org.Roclh.sh.scripts.CreateBandwidthRulesetScript;
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
        return createBandwidthRulesetScript;
    }



}
