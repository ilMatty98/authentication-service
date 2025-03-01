package com.ilmatty98.repository;

import com.ilmatty98.entity.Vault;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;
import java.util.Optional;

public class VaultRepository<Entity extends Vault> implements PanacheRepository<Entity> {

    public List<Entity> findAllByAccountId(Long accountId) {
        return find("account = ?1", accountId).list();
    }

    public Optional<Entity> findByIdAndAccountId(Long id, Long accountId) {
        return find("id = ?1 and account = ?2", id, accountId).firstResultOptional();
    }
}