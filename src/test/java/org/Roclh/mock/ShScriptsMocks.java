package org.Roclh.mock;

import org.Roclh.sh.scripts.CreateBandwidthRulesetScript;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
public class ShScriptsMocks {

    @MockBean
    private CreateBandwidthRulesetScript bandwidthRulesetScript;

    @Primary
    @Bean
    public CreateBandwidthRulesetScript createBandwidthRulesetScript() {
        Mockito.doNothing().when(bandwidthRulesetScript).init();
        Mockito.when(bandwidthRulesetScript.execute(Mockito.any())).then((ans) -> false);
        return bandwidthRulesetScript;
    }

}
