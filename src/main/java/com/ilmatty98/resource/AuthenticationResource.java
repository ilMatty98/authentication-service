package com.ilmatty98.resource;

import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.dto.request.ChangePasswordDto;
import com.ilmatty98.dto.request.DeleteDto;
import com.ilmatty98.dto.request.LogInDto;
import com.ilmatty98.dto.request.SignUpDto;
import com.ilmatty98.dto.response.AccessDto;
import com.ilmatty98.interceptor.BearerAuthenticated;
import com.ilmatty98.service.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Optional;

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

    @PUT
    @BearerAuthenticated
    @Path(CHANGE_PASSWORD)
    public boolean changePassword(
            @Valid @RequestBody ChangePasswordDto changePasswordDto,
            @Context ContainerRequestContext containerRequestContext) {
        var email = getEmailFromContext(containerRequestContext);
        return authenticationService.changePassword(changePasswordDto, email);
    }

    @POST
    @Path(SEND_HINT)
    public boolean sendHint(@RestPath String email) {
        return authenticationService.sendHint(email);
    }

    @DELETE
    @BearerAuthenticated
    @Path(DELETE_ACCOUNT)
    public boolean deleteAccount(
            @Valid @RequestBody DeleteDto deleteDto,
            @Context ContainerRequestContext containerRequestContext) {
        var email = getEmailFromContext(containerRequestContext);
        return authenticationService.deleteAccount(email, deleteDto);
    }

    private String getEmailFromContext(ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getProperty(TokenClaimEnum.EMAIL.getLabel()))
                .map(Object::toString)
                .orElseThrow(() -> new NotAuthorizedException("Missing email in request context"));
    }

}
