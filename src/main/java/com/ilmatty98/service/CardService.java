package com.ilmatty98.service;

import com.ilmatty98.dto.vault.CardDto;
import com.ilmatty98.entity.Card;
import com.ilmatty98.mapper.CredentialCardMapper;
import com.ilmatty98.repository.AccountRepository;
import com.ilmatty98.repository.CardRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CardService extends VaultService<Card, CardDto, CredentialCardMapper, CardRepository> {

    public CardService(CredentialCardMapper mapper, CardRepository repository, AccountRepository accountRepository) {
        super(mapper, repository, accountRepository);
    }
}
