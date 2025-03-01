package com.ilmatty98.dto.vault;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CardDto extends VaultDto {

    private String cardHolder;

    private String number;

    private LocalDate expiration;

    private String cvv;
}
