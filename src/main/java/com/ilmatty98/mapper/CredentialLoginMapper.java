package com.ilmatty98.mapper;

import com.ilmatty98.dto.credential.LoginDto;
import com.ilmatty98.entity.Login;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface CredentialLoginMapper extends CredentialMapper<Login, LoginDto> {
}
