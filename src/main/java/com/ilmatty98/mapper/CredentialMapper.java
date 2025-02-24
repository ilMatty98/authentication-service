package com.ilmatty98.mapper;

import com.ilmatty98.dto.credential.BaseDto;
import com.ilmatty98.entity.BaseCredential;
import org.mapstruct.Mapping;

public interface CredentialMapper<Entity extends BaseCredential, Dto extends BaseDto> {

    Dto entityToDto(Entity entity);

    @Mapping(target = "account", ignore = true)
    Entity dtoToEntity(Dto dto);

}
