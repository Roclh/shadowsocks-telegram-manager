package org.Roclh.util;


import org.Roclh.utils.PasswordUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordUtilsTest {
    @Test
    public void testValidation(){
        Assertions.assertTrue(PasswordUtils.validate("qwertyui"));
        Assertions.assertTrue(PasswordUtils.validate("123amigo"));
        Assertions.assertTrue(PasswordUtils.validate("subscribe$!"));
        Assertions.assertFalse(PasswordUtils.validate(""));
        Assertions.assertFalse(PasswordUtils.validate("some password"));
        Assertions.assertFalse(PasswordUtils.validate("Ilove'\""));
        Assertions.assertFalse(PasswordUtils.validate("никакойкириллицы"));
    }


}
