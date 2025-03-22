package com.ilmatty98.dto.vault;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CredentialDto extends VaultDto {

    private String site;

    private String username;

    private String password;
}
