package com.ilmatty98.service;

import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.vault.VaultDto;
import com.ilmatty98.entity.Vault;
import com.ilmatty98.mapper.CredentialMapper;
import com.ilmatty98.repository.AccountRepository;
import com.ilmatty98.repository.VaultRepository;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class VaultService<Entity extends Vault, Dto extends VaultDto,
        Mapper extends CredentialMapper<Entity, Dto>, Repository extends VaultRepository<Entity>> {

    private final Mapper mapper;
    private final Repository repository;
    private final AccountRepository accountRepository;

    public List<Dto> getAll(Long idAccount) {
        log.info("Init get credentials for account {}", idAccount);
        checkAccount(idAccount);
        var credentials = repository.findAllByAccountId(idAccount);

        log.info("End get credentials for account {}", idAccount);
        return credentials.stream()
                .map(mapper::entityToDto)
                .toList();
    }

    public Dto insert(Long idAccount, Dto dto) {
        log.info("Init insert credential for account {}", idAccount);
        checkAccount(idAccount);
        var entity = mapper.dtoToEntity(dto);
        repository.persist(entity);

        log.info("End insert credential for account {}", idAccount);
        return mapper.entityToDto(entity);
    }

    public Dto edit(Long idAccount, Dto dto) {
        log.info("Init edit credential for account {}", idAccount);
        checkAccount(idAccount);
        return repository.findByIdAndAccountId(dto.getId(), idAccount)
                .map(credential -> {
                    var updatedCredential = mapper.dtoToEntity(dto);
                    updatedCredential.setId(credential.getId());
                    repository.persist(updatedCredential);
                    log.info("End edit credential for account {}", idAccount);
                    return mapper.entityToDto(updatedCredential);
                })
                .orElseThrow(() -> {
                    log.warn("For account {}, credential {} not found", idAccount, dto.getId());
                    return new NotFoundException();
                });
    }

    public boolean delete(Long idAccount, Long idCredential) {
        log.info("Init delete credential for account {}", idAccount);
        checkAccount(idAccount);
        repository.findByIdAndAccountId(idCredential, idAccount)
                .ifPresentOrElse(repository::delete, () -> {
                    log.warn("For account {}, credential {} not found", idAccount, idCredential);
                    throw new NotFoundException();
                });
        log.info("End edit credential for account {}", idAccount);
        return true;
    }

    private void checkAccount(Long idAccount) {
        accountRepository.findByIdOptional(idAccount)
                .ifPresentOrElse(account -> {
                    if (AccountStateEnum.UNVERIFIED.equals(account.getState())) {
                        log.warn("Account id {} not verified", idAccount);
                        throw new BadRequestException();
                    }
                }, () -> {
                    log.warn("Account id {} not found", idAccount);
                    throw new NotFoundException();
                });
    }
}
