package com.ilmatty98.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordDto {

    @NotBlank(message = "CurrentMasterPasswordHash cannot be blank")
    private String currentMasterPasswordHash;

    @NotBlank(message = "NewMasterPasswordHash cannot be blank")
    private String newMasterPasswordHash;

    @NotBlank(message = "ProtectedSymmetricKey cannot be blank")
    private String newProtectedSymmetricKey;

    @NotBlank(message = "InitializationVector cannot be blank")
    private String newInitializationVector;
}
