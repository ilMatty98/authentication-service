package com.ilmatty98.dto.credential;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CardDto extends BaseDto {

    private String cardHolder;

    private String number;

    private LocalDate expiration;

    private String cvv;
}
