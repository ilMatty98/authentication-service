package com.ilmatty98.service;

import com.ilmatty98.dto.credential.BaseDto;
import com.ilmatty98.entity.BaseCredential;
import com.ilmatty98.mapper.CredentialMapper;
import com.ilmatty98.repository.BaseCredentialRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class CredentialService<Entity extends BaseCredential, Dto extends BaseDto,
        Mapper extends CredentialMapper<Entity, Dto>, Repository extends BaseCredentialRepository<Entity>> {

    public List<Dto> getAll(Long idAccount) {
        return null;
    }

    public Dto insert(Long idAccount, Dto baseDto) {
        return null;
    }

    public Dto edit(Long idAccount, Dto baseDto) {
        return null;
    }

    public boolean delete(Long idAccount, Long idCredential) {
        return false;
    }
}
