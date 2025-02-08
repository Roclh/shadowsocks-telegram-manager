package org.Roclh.sh.scripts;

import lombok.extern.slf4j.Slf4j;
import org.Roclh.sh.ScriptRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ScreenListScript extends AbstractShScript<List<String>> {

    private final Pattern regex = Pattern.compile("\\w+:\\w+");

    protected ScreenListScript() {
        super("screen_list.sh", """
                #!/bin/bash
                screen -wipe > /dev/null
                screen -ls
                
                """);
    }

    @Override
    public List<String> execute(String... args) {
        init();
        return ScriptRunner.runCommandWithResult(new String[]{"./" + fileName},
                commandOutput -> commandOutput.getStdOutputLines().stream().filter(regex.asPredicate())
                        .map(line -> {
                            Matcher matcher = regex.matcher(line);
                            matcher.find();
                            return matcher.group();
                        }).toList());
    }
}
