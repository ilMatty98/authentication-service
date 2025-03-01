package com.ilmatty98.mapper;

import com.ilmatty98.dto.vault.CredentialDto;
import com.ilmatty98.entity.Credential;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface CredentialLoginMapper extends CredentialMapper<Credential, CredentialDto> {
}
