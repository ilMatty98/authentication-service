package com.ilmatty98.mapper;

import com.ilmatty98.dto.vault.VaultDto;
import com.ilmatty98.entity.Vault;
import org.mapstruct.Mapping;

public interface CredentialMapper<Entity extends Vault, Dto extends VaultDto> {

    Dto entityToDto(Entity entity);

    @Mapping(target = "account", ignore = true)
    Entity dtoToEntity(Dto dto);

}
