package org.Roclh.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Builder
@AllArgsConstructor
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_model_gen")
    @SequenceGenerator(name = "user_model_gen", sequenceName = "user_model_seq")
    @Column(name = "id", nullable = false)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_model_id")
    private TelegramUserModel userModel;
    @Nullable
    private String password;
    @NotNull
    private Plugin plugin = Plugin.DEFAULT;
    @Nullable
    private Long usedPort;
    private boolean isAdded;

    @Override
    public String toString() {
        return "[" + userModel.getTelegramId() + ":'" + userModel.getTelegramName() + "']: " +
                " usedPort='" + usedPort + '\'' +
                ", isAdded=" + isAdded +
                ", password=" + password;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserModel userModel = (UserModel) o;
        return getId() != null && Objects.equals(getId(), userModel.getId());
    }

    public String toFormattedString() {
        return "<b><i>" + userModel.getTelegramName() + ":</i></b>\n<u>Telegram Id:</u> " +
                "<a href=\"tg://user?id=" + userModel.getTelegramId() + "\">" + userModel.getTelegramId() + "</a>" +
                "\n<u>Used port</u>: " + usedPort +
                "\n<u>Password</u>: <tg-spoiler>" + password + "</tg-spoiler>"+
                "\n<u>Is added</u>: " + isAdded +
                "\n<u>Plugin</u>: " + plugin;
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public enum Plugin {
        DEFAULT, V2RAY
    }
}
