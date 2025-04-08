package com.paymentservice.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table("balances")
public class UserBalance {

    @Id
    private Integer id;

    private Double balance;
}
