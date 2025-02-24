package com.ilmatty98.service;

import com.ilmatty98.dto.credential.BaseDto;

import java.util.List;


public interface CredentialService<Dto extends BaseDto> {

    List<Dto> getAll(String idAccount);

    Dto save(String idAccount, Dto baseDto);

    boolean delete(String idAccount, Dto baseDto);
}
