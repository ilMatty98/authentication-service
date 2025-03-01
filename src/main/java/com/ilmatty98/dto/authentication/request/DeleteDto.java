package com.ilmatty98.dto.authentication.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteDto {

    @NotBlank(message = "MasterPasswordHash cannot be blank")
    private String masterPasswordHash;
}
