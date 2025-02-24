package com.ilmatty98.resource;

import com.ilmatty98.dto.credential.CardDto;
import com.ilmatty98.entity.Card;
import com.ilmatty98.mapper.CredentialCardMapper;
import com.ilmatty98.repository.CardRepository;
import com.ilmatty98.service.CredentialServiceCard;
import jakarta.ws.rs.Path;

import static com.ilmatty98.constants.UrlConstants.Card.BASE_PATH_CARD;

@Path(BASE_PATH_CARD)
public class CardResource extends CredentialResource<Card, CardDto, CredentialCardMapper, CardRepository,
        CredentialServiceCard> {

    public CardResource(CredentialServiceCard credentialService) {
        super(credentialService);
    }
}
