package com.ilmatty98.service;

import com.ilmatty98.dto.credential.LoginDto;
import com.ilmatty98.repository.LoginRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;

import java.util.List;

@Data
@ApplicationScoped
public class CredentialServiceLoginImpl implements CredentialService<LoginDto> {

    private final LoginRepository loginRepository;

    @Override
    public List<LoginDto> getAll(Long idAccount) {
        return null;
    }

    @Override
    public LoginDto insert(Long idAccount, LoginDto baseDto) {
        return null;
    }

    @Override
    public LoginDto edit(Long idAccount, LoginDto baseDto) {
        return null;
    }

    @Override
    public boolean delete(Long idAccount, Long idCredential) {
        return false;
    }
}
