package com.ilmatty98.utils;

import com.ilmatty98.entity.Credential;
import com.ilmatty98.repository.CredentialRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/credential")
@RequiredArgsConstructor
public class CredentialResource {

    private final CredentialRepository credentialRepository;

    @GET
    @Path("/{id}")
    public Credential get(@PathParam("id") Long id) {
        return credentialRepository.findById(id);
    }

    @POST
    @Transactional
    public Credential save(@RequestBody Credential credential) {
        credential.setId(null);
        credentialRepository.persist(credential);
        return credential;
    }

    @POST
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        credentialRepository.deleteById(id);
    }
}
