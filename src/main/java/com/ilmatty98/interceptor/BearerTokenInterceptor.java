package com.ilmatty98.interceptor;

import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.service.TokenJwtService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;


@Interceptor
@BearerAuthenticated
public class BearerTokenInterceptor {

    @Inject
    TokenJwtService tokenJwtService;

    @Context
    ContainerRequestContext containerRequestContext;

    @AroundInvoke
    public Object validateBearerToken(InvocationContext context) throws Exception {
        var authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            throw new NotAuthorizedException("Missing or invalid Authorization header");

        var token = authorizationHeader.substring("Bearer".length()).trim();

        // Verify the token
        var claims = tokenJwtService.validateTokenJwt(token);
        var email = claims.get(TokenClaimEnum.EMAIL.getLabel()).toString();

        // Sets the email extracted from the token in the header
        containerRequestContext.setProperty(TokenClaimEnum.EMAIL.getLabel(), email);

        // If the token is valid, the flow continues execution
        return context.proceed();
    }
}
