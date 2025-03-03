package org.Roclh.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.Roclh.data.enums.Role;
import org.springframework.lang.Nullable;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramUserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    @NonNull
    private Role role;

    @NonNull
    @Column(unique = true)
    private Long telegramId;
    @Nullable
    private String telegramName;
    @Nullable
    private Long chatId;

    @Override
    public String toString() {
        return "[" + telegramId + ":'" + telegramName + " - " + role.name() + "']: " +
                " chatId='" + chatId + '\'';
    }

    public String toFormattedString() {
        return "<b><i>" + telegramName + ":</i></b>\n<u>Telegram Id:</u> " +
                "<a href=\"tg://user?id=" + telegramId + "\">" + telegramId + "</a>" +
                "\n<u>Role:</u> " + role.name() +
                "\n<u>Chat Id:</u> " + chatId +
                "\n";
    }

}
