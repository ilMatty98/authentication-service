package com.ilmatty98.dto.credential;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginDto extends BaseDto {

    private String site;

    private String username;

    private String password;
}
