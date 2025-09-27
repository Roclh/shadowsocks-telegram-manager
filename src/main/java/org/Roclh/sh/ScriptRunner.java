package org.Roclh.sh;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ScriptRunner {

    public static boolean isShScriptExists(String scriptPath) {
        return Files.exists(Path.of(scriptPath));
    }

    public static void createShScript(String scriptContent, String scriptPath) {
        try (FileWriter fileWriter = new FileWriter(scriptPath)) {
            fileWriter.write(scriptContent);
            log.debug("Changing file permissons for {}", scriptPath);
            Files.setPosixFilePermissions(Path.of(scriptPath), PosixFilePermissions.fromString("rwxrwxrwx"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean runCommand(String[] command) {
        return runCommandWithResult(command, (output) -> true, () -> false);
    }


    public static boolean runCommand(String[] command, Predicate<CommandOutput> successCondition) {
        return runCommandWithResult(command, successCondition::test, () -> false);
    }

    @Nullable
    public static String runCommandWithResult(String[] command) {
        return runCommandWithResult(command, CommandOutput::get);
    }

    @Nullable
    public static <T> T runCommandWithResult(String[] command, Function<CommandOutput, T> resultProcessor) {
        return runCommandWithResult(command, resultProcessor, () -> null);
    }

    public static <T> T runCommandWithResult(String[] command, Function<CommandOutput, T> resultProcessor, Supplier<T> fallbackProvider) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        log.debug("Executing command {}, command with args: {}", command[0], command);
        try {
            Process p = processBuilder.start();
            CommandOutput output = new CommandOutput();
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            stdInput.lines().forEach(line -> {
                log.info(line);
                output.appendOutput(line);
            });

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));
            stdError.lines().forEach(line -> {
                log.error(line);
                output.appendError(line);
            });
            p.onExit().get(10, TimeUnit.SECONDS);
            return resultProcessor.apply(output);
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Failed to execute script {}", String.join(" ", command), e);
            return fallbackProvider.get();
        }
    }

    @Getter
    public static class CommandOutput {
        List<String> stdOutputLines = new CopyOnWriteArrayList<>();
        List<String> stdErrorLines = new CopyOnWriteArrayList<>();

        public void appendOutput(@NonNull String line) {
            this.stdOutputLines.add(line);
        }

        public void appendError(@NonNull String line) {
            this.stdErrorLines.add(line);
        }

        public boolean contains(String line) {
            return stdOutputLines.stream().anyMatch(output -> output.contains(line));
        }

        public boolean containsError(String error) {
            return stdErrorLines.stream().anyMatch(output -> output.contains(error));
        }

        public boolean hasErrors() {
            return !stdErrorLines.isEmpty();
        }

        public boolean isEmpty() {
            return stdErrorLines.isEmpty() && stdOutputLines.isEmpty();
        }

        public String getOutput() {
            return String.join("\n", this.stdOutputLines);
        }

        public String getError() {
            return String.join("\n", this.stdErrorLines);
        }

        public String get() {
            return Stream.concat(
                            stdOutputLines.stream(),
                            stdErrorLines.stream())
                    .collect(Collectors.joining("\n"));
        }
    }
}
