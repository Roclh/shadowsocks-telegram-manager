package org.Roclh.sh.scripts;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.sh.ScriptRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateBandwidthRulesetScript extends AbstractShScript<Boolean> {
    boolean wasExecuted = false;
    boolean recordedResult = false;

    protected CreateBandwidthRulesetScript() {
        super("create_ruleset.sh", """
                #!/bin/bash
                
                tc qdisc add dev eth0 root handle 1: prio
                
                """);
    }

    @Override
    public Boolean execute(String... args) {
        if (wasExecuted) {
            return recordedResult;
        }
        init();
        this.wasExecuted = true;
        this.recordedResult = ScriptRunner.runCommand(new String[]{"./" + fileName},
                output -> output.contains("Error: Specified qdisc kind is unknown"));
        return recordedResult;
    }
}
