package com.ilmatty98.dto.request;

import com.ilmatty98.dto.BaseDto;
import com.ilmatty98.validator.MaxSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SignUpDto extends BaseDto {

    @NotBlank(message = "MasterPasswordHash cannot be blank")
    private String masterPasswordHash;

    @NotBlank(message = "ProtectedSymmetricKey cannot be blank")
    private String protectedSymmetricKey;

    @NotBlank(message = "InitializationVector cannot be blank")
    private String initializationVector;

    @Pattern(message = "Language is not valid", regexp = "^[A-Z]{2}$")
    @NotBlank(message = "Language cannot be blank")
    private String language;

    @Size(max = 100)
    @NotBlank(message = "Hint cannot be blank")
    private String hint;

    @NotBlank(message = "Propic cannot be blank")
    @MaxSize(value = 4 * 1024 * 1024, message = "Propic size exceeds the maximum limit of 4 MB")
    private String propic;

}
