package com.ilmatty98.utils;

import com.ilmatty98.entity.Account;
import com.ilmatty98.repository.AccountRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/account")
@RequiredArgsConstructor
public class AccountResource {

    private final AccountRepository accountRepository;

    @GET
    @Path("/{id}")
    public Account getAccount(@PathParam("id") Long id) {
        return accountRepository.findById(id);
    }

    @POST
    @Transactional
    public Account saveAccount(@RequestBody Account account) {
        account.setId(null);
        accountRepository.persist(account);
        return account;
    }

    @PUT
    @Transactional
    public Account editAccount(@RequestBody Account account) {
        accountRepository.getEntityManager().merge(account);
        return account;
    }

    @POST
    @Path("/{id}")
    @Transactional
    public void deleteAccountById(@PathParam("id") Long id) {
        accountRepository.deleteById(id);
    }
}
