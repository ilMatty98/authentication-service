package com.ilmatty98.resource;

import com.ilmatty98.dto.request.LogInDto;
import com.ilmatty98.dto.request.SignUpDto;
import com.ilmatty98.dto.response.AccessDto;
import com.ilmatty98.service.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestPath;

import static com.ilmatty98.constants.UrlConstants.*;

@RequiredArgsConstructor
@Path(BASE_PATH_AUTHENTICATION)
public class AuthenticationResource {

    private final AuthenticationService authenticationService;

    @POST
    @Path(SIGN_UP)
    public boolean signUp(@Valid @RequestBody SignUpDto signUpDto) {
        return authenticationService.signUp(signUpDto);
    }

    @POST
    @Path(LOG_IN)
    public AccessDto logIn(@Valid @RequestBody LogInDto logInDto) {
        return authenticationService.logIn(logInDto);
    }

    @GET
    @Path(CHECK_EMAIL)
    public boolean checkEmail(@RestPath String email) {
        return authenticationService.checkEmail(email);
    }

    @PATCH
    @Path(CONFIRM_EMAIL)
    public boolean confirmEmail(@RestPath String email, @RestPath String code) {
        return authenticationService.confirmEmail(email, code);
    }

}
