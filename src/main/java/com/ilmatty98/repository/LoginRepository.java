package com.ilmatty98.repository;

import com.ilmatty98.entity.Login;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LoginRepository extends BaseCredentialRepository<Login> {
}