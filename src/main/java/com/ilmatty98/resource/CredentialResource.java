package com.ilmatty98.resource;

import com.ilmatty98.dto.vault.CredentialDto;
import com.ilmatty98.entity.Credential;
import com.ilmatty98.mapper.CredentialLoginMapper;
import com.ilmatty98.repository.CredentialRepository;
import com.ilmatty98.service.CredentialService;
import jakarta.ws.rs.Path;

import static com.ilmatty98.constants.UrlConstants.Credential.BASE_PATH_CREDENTIAL;

@Path(BASE_PATH_CREDENTIAL)
public class CredentialResource extends VaultResource<Credential, CredentialDto, CredentialLoginMapper, CredentialRepository,
        CredentialService> {

    public CredentialResource(CredentialService credentialService) {
        super(credentialService);
    }

}
