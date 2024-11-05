package com.ilmatty98.resource;

import com.ilmatty98.dto.request.SignUpDto;
import com.ilmatty98.service.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import static com.ilmatty98.constants.UrlConstants.BASE_PATH_AUTHENTICATION;
import static com.ilmatty98.constants.UrlConstants.SIGN_UP;

@RequiredArgsConstructor
@Path(BASE_PATH_AUTHENTICATION)
public class AuthenticationResource {

    private final AuthenticationService authenticationService;

    @POST
    @Path(SIGN_UP)
    public void signUp(@Valid @RequestBody SignUpDto signUpDto) {
        authenticationService.signUp(signUpDto);
    }


}
