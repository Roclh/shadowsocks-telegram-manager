package org.Roclh.ss;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import javax.validation.constraints.NotBlank;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Configuration
@Data
@ConfigurationProperties(prefix = "shadowsocks")
@Slf4j
public class ShadowsocksProperties {
    @NotBlank
    private String address;
    @NotBlank
    private String availablePorts;
    @NotBlank
    private String defaultMethod;
    @NotBlank
    private Long testPort;

    @EventListener(ContextRefreshedEvent.class)
    @Order(0)
    public void init() {
        createDefaultConfig();
        createConfigWithV2RayPlugin();
    }

    private void createDefaultConfig(){
        try (FileWriter fileWriter = new FileWriter("/etc/shadowsocks-libev/config-example.json")) {
            String configuration = """
                    {
                        "server":"${address}",
                        "mode":"tcp_and_udp",
                        "server_port":8488,
                        "local_port":1080,
                        "password":"123456",
                        "timeout":60,
                        "method":"${method}"
                    }
                    """;
            configuration = configuration.replace("${address}", this.address);
            configuration = configuration.replace("${method}", this.defaultMethod);
            fileWriter.write(configuration);
            log.info("Created a configuration file with content {}", configuration);
        } catch (IOException e) {
            log.error("Failed to create a file", e);
        }
    }

    private void createConfigWithV2RayPlugin(){
        try (FileWriter fileWriter = new FileWriter("/etc/shadowsocks-libev/config-v2ray-example.json")) {
            String configuration = """
                    {
                        "server":"${address}",
                        "mode":"tcp_and_udp",
                        "server_port":8488,
                        "local_port":1080,
                        "password":"123456",
                        "timeout":60,
                        "method":"${method}",
                        "plugin":"v2ray-plugin",
                        "plugin_opts":"server"
                    }
                    """;
            configuration = configuration.replace("${address}", this.address);
            configuration = configuration.replace("${method}", this.defaultMethod);
            fileWriter.write(configuration);
            log.info("Created a configuration file with content {}", configuration);
        } catch (IOException e) {
            log.error("Failed to create a file", e);
        }
    }
    public PortsRange getPortRange(){
        return PortsRange.from(availablePorts);
    }


    @Getter
    public static class PortsRange{
        private final long leftRangeLimit;
        private final long rightRangeLimit;

        private PortsRange(long leftRangeLimit, long rightRangeLimit){
            this.leftRangeLimit = leftRangeLimit;
            this.rightRangeLimit = rightRangeLimit;
        }

        public List<Long> range(){
            return LongStream.range(leftRangeLimit, rightRangeLimit).boxed().collect(Collectors.toList());
        }

        public static PortsRange from(String port){
            String[] portLimits = port.split(":");
            return new PortsRange(Long.parseLong(portLimits[0]), Long.parseLong(portLimits[1]));
        }
    }
}
