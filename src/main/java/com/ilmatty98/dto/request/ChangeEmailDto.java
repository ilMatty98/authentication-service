package com.ilmatty98.dto.request;

import com.ilmatty98.dto.BaseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class ChangeEmailDto extends BaseDto {

    @NotBlank(message = "MasterPasswordHash cannot be blank")
    private String masterPasswordHash;

}
