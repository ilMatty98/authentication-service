package com.ilmatty98.service;

import com.ilmatty98.dto.vault.CredentialDto;
import com.ilmatty98.entity.Credential;
import com.ilmatty98.mapper.CredentialLoginMapper;
import com.ilmatty98.repository.AccountRepository;
import com.ilmatty98.repository.CredentialRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialService extends VaultService<Credential, CredentialDto, CredentialLoginMapper, CredentialRepository> {

    public CredentialService(CredentialLoginMapper mapper, CredentialRepository repository, AccountRepository accountRepository) {
        super(mapper, repository, accountRepository);
    }
}
