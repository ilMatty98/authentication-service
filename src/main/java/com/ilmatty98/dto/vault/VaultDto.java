package com.ilmatty98.dto.vault;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Timestamp;

@Data
public abstract class VaultDto {

    private Long id;

    @NotBlank
    private String name;

    private String notes;

    private Timestamp timestampCreation;

    private Timestamp timestampUpdated;
}
