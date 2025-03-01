package com.ilmatty98.resource;

import com.ilmatty98.dto.vault.CardDto;
import com.ilmatty98.entity.Card;
import com.ilmatty98.mapper.CredentialCardMapper;
import com.ilmatty98.repository.CardRepository;
import com.ilmatty98.service.CardService;
import jakarta.ws.rs.Path;

import static com.ilmatty98.constants.UrlConstants.Card.BASE_PATH_CARD;

@Path(BASE_PATH_CARD)
public class CardResource extends VaultResource<Card, CardDto, CredentialCardMapper, CardRepository,
        CardService> {

    public CardResource(CardService credentialService) {
        super(credentialService);
    }

    public CardResource() {
        super(null);
    }
}
