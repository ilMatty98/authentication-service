package com.ilmatty98.resource;

import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.dto.credential.BaseDto;
import com.ilmatty98.interceptor.BearerAuthenticated;
import com.ilmatty98.service.CredentialService;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class CredentialResource<Dto extends BaseDto, Service extends CredentialService<Dto>> {

    private final Service credentialService;

    @GET
    @BearerAuthenticated
    public List<Dto> getAll(@Context ContainerRequestContext containerRequestContext) {
        var id = getAccountIdFromContext(containerRequestContext);
        return credentialService.getAll(id);
    }

    @POST
    @BearerAuthenticated
    public Dto save(@Valid @RequestBody Dto cardDto,
                    @Context ContainerRequestContext containerRequestContext) {
        //TODO: applicare il validator su questa regola
        if (cardDto.getId() == null) throw new BadRequestException();
        var id = getAccountIdFromContext(containerRequestContext);
        return credentialService.save(id, cardDto);
    }

    @DELETE
    @BearerAuthenticated
    public boolean delete(@Valid @RequestBody Dto cardDto,
                          @Context ContainerRequestContext containerRequestContext) {
        //TODO: applicare il validator su questa regola
        if (cardDto.getId() == null) throw new BadRequestException();
        var id = getAccountIdFromContext(containerRequestContext);
        return credentialService.delete(id, cardDto);
    }

    private String getAccountIdFromContext(ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getProperty(TokenClaimEnum.ID.getLabel()))
                .map(Object::toString)
                .orElseThrow(() -> new NotAuthorizedException("Missing id in request context"));
    }

}
