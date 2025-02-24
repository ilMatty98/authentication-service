package com.ilmatty98.service;

import com.ilmatty98.dto.credential.BaseDto;

import java.util.List;


public interface CredentialService<Dto extends BaseDto> {

    List<Dto> getAll(Long idAccount);

    Dto insert(Long idAccount, Dto baseDto);

    Dto edit(Long idAccount, Dto baseDto);

    boolean delete(Long idAccount, Long idCredential);

}
