package com.ilmatty98.resource;

import com.ilmatty98.dto.credential.LoginDto;
import com.ilmatty98.service.CredentialServiceLoginImpl;
import jakarta.ws.rs.Path;

import static com.ilmatty98.constants.UrlConstants.Login.BASE_PATH_LOGIN;

@Path(BASE_PATH_LOGIN)
public class LoginResource extends CredentialResource<LoginDto, CredentialServiceLoginImpl> {


    public LoginResource(CredentialServiceLoginImpl credentialService) {
        super(credentialService);
    }
}
