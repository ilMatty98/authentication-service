package com.ilmatty98.dto.vault;

import com.ilmatty98.validator.ValidationCredential;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.sql.Timestamp;

@Data
public abstract class VaultDto {

    @Null(groups = ValidationCredential.Post.class)
    @NotNull(groups = ValidationCredential.Put.class)
    private Long id;

    @NotBlank
    private String name;

    private String notes;

    private Timestamp timestampCreation;

    private Timestamp timestampUpdated;
}
