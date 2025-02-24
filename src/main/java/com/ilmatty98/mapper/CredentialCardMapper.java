package com.ilmatty98.mapper;

import com.ilmatty98.dto.credential.CardDto;
import com.ilmatty98.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface CredentialCardMapper extends CredentialMapper<Card, CardDto> {
}
