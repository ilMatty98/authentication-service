package com.ilmatty98.resource;

import com.ilmatty98.dto.credential.LoginDto;
import com.ilmatty98.entity.Login;
import com.ilmatty98.mapper.CredentialLoginMapper;
import com.ilmatty98.repository.LoginRepository;
import com.ilmatty98.service.CredentialServiceLogin;
import jakarta.ws.rs.Path;

import static com.ilmatty98.constants.UrlConstants.Login.BASE_PATH_LOGIN;

@Path(BASE_PATH_LOGIN)
public class LoginResource extends CredentialResource<Login, LoginDto, CredentialLoginMapper, LoginRepository,
        CredentialServiceLogin> {

    public LoginResource(CredentialServiceLogin credentialService) {
        super(credentialService);
    }
}
