package com.ilmatty98.service;

import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.vault.VaultDto;
import com.ilmatty98.entity.Account;
import com.ilmatty98.entity.Vault;
import com.ilmatty98.mapper.CredentialMapper;
import com.ilmatty98.repository.AccountRepository;
import com.ilmatty98.repository.VaultRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public abstract class VaultService<Entity extends Vault, Dto extends VaultDto,
        Mapper extends CredentialMapper<Entity, Dto>, Repository extends VaultRepository<Entity>> {

    private Mapper mapper;
    private Repository repository;
    private AccountRepository accountRepository;

    public List<Dto> getAll(Long idAccount) {
        log.info("Init get credentials for account {}", idAccount);
        checkAccount(idAccount);
        var credentials = repository.findAllByAccountId(idAccount);

        log.info("End get credentials for account {}", idAccount);
        return credentials.stream()
                .map(mapper::entityToDto)
                .toList();
    }

    @Transactional
    public Dto insert(Long idAccount, Dto dto) {
        log.info("Init insert credential for account {}", idAccount);
        var account = checkAccount(idAccount);

        var entity = mapper.dtoToEntity(dto);
        entity.setAccount(account);
        repository.persist(entity);

        log.info("End insert credential for account {}", idAccount);
        return mapper.entityToDto(entity);
    }

    @Transactional
    public Dto edit(Long idAccount, Dto dto) {
        log.info("Init edit credential for account {}", idAccount);
        var account = checkAccount(idAccount);

        return repository.findByIdAndAccountId(dto.getId(), account.getId())
                .map(credential -> {
                    var updatedCredential = mapper.dtoToEntity(dto);
                    updatedCredential.setId(credential.getId());
                    updatedCredential.setAccount(account);

                    repository.getEntityManager().merge(updatedCredential);
                    log.info("End edit credential for account {}", idAccount);
                    return mapper.entityToDto(updatedCredential);
                })
                .orElseThrow(() -> {
                    log.warn("For account {}, credential {} not found", idAccount, dto.getId());
                    return new NotFoundException();
                });
    }

    @Transactional
    public boolean delete(Long idAccount, Long idCredential) {
        log.info("Init delete credential for account {}", idAccount);
        var account = checkAccount(idAccount);

        repository.findByIdAndAccountId(idCredential, account.getId())
                .ifPresentOrElse(repository::delete, () -> {
                    log.warn("For account {}, credential {} not found", idAccount, idCredential);
                    throw new NotFoundException();
                });
        log.info("End edit credential for account {}", idAccount);
        return true;
    }

    private Account checkAccount(Long idAccount) {
        return accountRepository.findByIdOptional(idAccount)
                .map(account -> {
                    if (AccountStateEnum.UNVERIFIED.equals(account.getState())) {
                        log.warn("Account id {} not verified", idAccount);
                        throw new BadRequestException();
                    }
                    return account;
                })
                .orElseThrow(() -> {
                    log.warn("Account id {} not found", idAccount);
                    return new NotFoundException();
                });
    }
}
