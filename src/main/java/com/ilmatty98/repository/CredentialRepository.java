package com.ilmatty98.repository;

import com.ilmatty98.entity.Credential;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialRepository extends VaultRepository<Credential> {
}