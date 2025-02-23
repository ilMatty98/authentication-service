package com.ilmatty98;

import com.ilmatty98.entity.Account;
import com.ilmatty98.repository.AccountRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestPath;

@Path("/account")
@RequiredArgsConstructor
public class UpdateResource {


    private final AccountRepository accountRepository;

    @GET
    @Path("/{id}")
    public Account getAccount(@RestPath Long id) {
        return accountRepository.findById(id);
    }

    @POST
    @Transactional
    public Account saveAccount(@RequestBody Account account) {
        account.setId(null);
        accountRepository.persist(account);
        return account;
    }

    @POST
    @Path("/{id}")
    @Transactional
    public void deleteAccountById(@RestPath Long id) {
        accountRepository.deleteById(id);
    }
}
