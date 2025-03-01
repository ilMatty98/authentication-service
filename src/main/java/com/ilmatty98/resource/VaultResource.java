package com.ilmatty98.resource;

import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.dto.vault.VaultDto;
import com.ilmatty98.entity.Vault;
import com.ilmatty98.interceptor.BearerAuthenticated;
import com.ilmatty98.mapper.CredentialMapper;
import com.ilmatty98.repository.VaultRepository;
import com.ilmatty98.service.VaultService;
import com.ilmatty98.validator.ValidationCredential;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class VaultResource<Entity extends Vault, Dto extends VaultDto,
        Mapper extends CredentialMapper<Entity, Dto>, Repository extends VaultRepository<Entity>,
        Service extends VaultService<Entity, Dto, Mapper, Repository>> {

    private final Service credentialService;

    @GET
    @BearerAuthenticated
    public List<Dto> getAll(@Context ContainerRequestContext containerRequestContext) {
        var idAccount = getAccountIdFromContext(containerRequestContext);
        return credentialService.getAll(idAccount);
    }

    @POST
    @BearerAuthenticated
    public Dto insert(@Valid @ConvertGroup(to = ValidationCredential.Post.class) @RequestBody Dto baseDto,
                      @Context ContainerRequestContext containerRequestContext) {
        var idAccount = getAccountIdFromContext(containerRequestContext);
        return credentialService.insert(idAccount, baseDto);
    }

    @PUT
    @BearerAuthenticated
    public Dto edit(@Valid @ConvertGroup(to = ValidationCredential.Put.class) @RequestBody Dto baseDto,
                    @Context ContainerRequestContext containerRequestContext) {
        var idAccount = getAccountIdFromContext(containerRequestContext);
        return credentialService.edit(idAccount, baseDto);
    }

    @DELETE
    @Path("/{idCredential}")
    @BearerAuthenticated
    public boolean delete(@PathParam("idCredential") @NotNull Long idCredential,
                          @Context ContainerRequestContext containerRequestContext) {
        var idAccount = getAccountIdFromContext(containerRequestContext);
        return credentialService.delete(idAccount, idCredential);
    }

    private Long getAccountIdFromContext(ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getProperty(TokenClaimEnum.ID.getLabel()))
                .map(obj -> Long.valueOf(obj.toString()))
                .orElseThrow(() -> new NotAuthorizedException("Missing id in request context"));
    }

}
