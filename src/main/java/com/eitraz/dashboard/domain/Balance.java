package com.eitraz.dashboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "balance")
@Getter
@Setter
@Accessors(chain = true)
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 64)
    @Column(name = "account_id")
    private String accountId;

    @NotBlank
    @Size(min = 1, max = 128)
    private String name;

    @NotNull
    private BigDecimal balance;

    @Column(insertable = false, updatable = false)
    private Timestamp timestamp;
}
