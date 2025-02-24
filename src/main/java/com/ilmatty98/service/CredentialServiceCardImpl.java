package com.ilmatty98.service;

import com.ilmatty98.dto.credential.CardDto;
import com.ilmatty98.repository.CardRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;

import java.util.List;

@Data
@ApplicationScoped
public class CredentialServiceCardImpl implements CredentialService<CardDto> {

    private final CardRepository cardRepository;

    @Override
    public List<CardDto> getAll(Long idAccount) {
        return null;
    }

    @Override
    public CardDto insert(Long idAccount, CardDto baseDto) {
        return null;
    }

    @Override
    public CardDto edit(Long idAccount, CardDto baseDto) {
        return null;
    }

    @Override
    public boolean delete(Long idAccount, Long idCredential) {
        return false;
    }
}
