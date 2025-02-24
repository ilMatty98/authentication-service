package com.ilmatty98.service;

import com.ilmatty98.dto.credential.LoginDto;
import com.ilmatty98.entity.Login;
import com.ilmatty98.mapper.CredentialLoginMapper;
import com.ilmatty98.repository.LoginRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialServiceLogin extends CredentialService<Login, LoginDto, CredentialLoginMapper, LoginRepository> {
}
