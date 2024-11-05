package com.ilmatty98.dto.request;

import com.ilmatty98.dto.BaseDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogInDto extends BaseDto {

    @NotBlank(message = "MasterPasswordHash cannot be blank")
    private String masterPasswordHash;

    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    @NotBlank(message = "IpAddress cannot be blank")
    private String ipAddress;

    @NotBlank(message = "DeviceType cannot be blank")
    private String deviceType;

    @NotBlank(message = "LocalDateTime cannot be blank")
    private String localDateTime;
}
