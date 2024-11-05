package com.ilmatty98.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteDto {

    @NotBlank(message = "CurrentMasterPasswordHash cannot be blank")
    private String masterPasswordHash;
}
