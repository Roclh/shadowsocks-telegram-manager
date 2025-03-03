package org.Roclh.data.enums;

import java.util.Arrays;

public enum Role {
    GUEST(0), USER(1), MANAGER(2), ROOT(3);

    public final int prior;

    Role(int prior){
        this.prior = prior;
    }

    public Role up(){
        return Arrays.stream(Role.values()).filter(role -> role.prior == this.prior + 1).findFirst().orElse(Role.ROOT);
    }

}
