package org.Roclh.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.Roclh.data.enums.Bandwidth;
import org.springframework.lang.Nullable;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BandwidthModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    @NonNull
    @OneToOne
    @JoinColumn(name = "user_model_id")
    private UserModel userModel;

    /**
     * Contains bandwidth for user, null means no bandwidth limitations
     */
    @Nullable
    private Bandwidth bandwidth;

    public String toFormattedString(){
        return "\n<u>Bandwidth</u>:" + bandwidth;
    }

}
