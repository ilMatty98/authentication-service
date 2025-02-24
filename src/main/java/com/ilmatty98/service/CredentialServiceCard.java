package com.ilmatty98.service;

import com.ilmatty98.dto.credential.CardDto;
import com.ilmatty98.entity.Card;
import com.ilmatty98.mapper.CredentialCardMapper;
import com.ilmatty98.repository.CardRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialServiceCard extends CredentialService<Card, CardDto, CredentialCardMapper, CardRepository> {
}
