package com.ilmatty98.resource;

import com.ilmatty98.dto.credential.CardDto;
import com.ilmatty98.service.CredentialServiceCardImpl;
import jakarta.ws.rs.Path;

import static com.ilmatty98.constants.UrlConstants.Card.BASE_PATH_CARD;

@Path(BASE_PATH_CARD)
public class CardResource extends CredentialResource<CardDto, CredentialServiceCardImpl> {

    public CardResource(CredentialServiceCardImpl credentialService) {
        super(credentialService);
    }
}
