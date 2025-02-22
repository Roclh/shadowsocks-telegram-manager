package org.Roclh.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

@Slf4j
public class PasswordUtils {
    private static final String usedSymbols = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final Pattern passwordPattern = Pattern.compile("[0-9a-zA-Z!$#^~_]{5,15}");

    public static boolean validate(String password) {
        return passwordPattern.matcher(password).matches();
    }

    public static Optional<String> md5(String password) {
        return Optional.ofNullable(DigestUtils.md5(StringUtils.getBytes(password, Charset.forName("windows-1251")))).map(bytes -> {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            return Stream.generate(byteBuffer::get).limit(byteBuffer.capacity())
                    .map(b -> usedSymbols.charAt(Math.abs(b) % usedSymbols.length()))
                    .collect(Collector.of(
                            StringBuilder::new,
                            StringBuilder::append,
                            StringBuilder::append,
                            StringBuilder::toString
                    ));
        });
    }
}
