package com.ilmatty98.resource;

import com.ilmatty98.dto.request.LogInDto;
import com.ilmatty98.dto.request.SignUpDto;
import com.ilmatty98.dto.response.AccessDto;
import com.ilmatty98.service.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import static com.ilmatty98.constants.UrlConstants.*;

@RequiredArgsConstructor
@Path(BASE_PATH_AUTHENTICATION)
public class AuthenticationResource {

    private final AuthenticationService authenticationService;

    @POST
    @Path(SIGN_UP)
    public void signUp(@Valid @RequestBody SignUpDto signUpDto) {
        authenticationService.signUp(signUpDto);
    }

    @POST
    @Path(LOG_IN)
    public AccessDto logIn(@Parameter(description = "Dto to log in") @Valid @RequestBody LogInDto logInDto) {
        return authenticationService.logIn(logInDto);
    }

}
