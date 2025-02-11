package org.Roclh.data.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Builder
@AllArgsConstructor
public class ContractModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_model_id")
    private UserModel userModel;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean wasNotified = false;

    public String toFormattedString(){
        return  "\n<u>Start date</u>: " + startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                "\n<u>End date</u>: " + endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
