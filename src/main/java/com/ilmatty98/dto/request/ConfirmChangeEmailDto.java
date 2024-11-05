package com.ilmatty98.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class ConfirmChangeEmailDto extends ChangeEmailDto {

    @NotBlank(message = "VerificationCode cannot be blank")
    private String verificationCode;

    @NotBlank(message = "NewMasterPasswordHash cannot be blank")
    private String newMasterPasswordHash;

    @NotBlank(message = "NewProtectedSymmetricKey cannot be blank")
    private String newProtectedSymmetricKey;

    @NotBlank(message = "NewInitializationVector cannot be blank")
    private String newInitializationVector;

}
