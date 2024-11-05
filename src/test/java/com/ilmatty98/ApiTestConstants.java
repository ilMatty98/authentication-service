package com.ilmatty98;

import lombok.NoArgsConstructor;

import static com.ilmatty98.constants.UrlConstants.*;

@NoArgsConstructor
public abstract class ApiTestConstants {

    protected static final String CONFIRM_CHANGE_EMAIL_URL = BASE_PATH_AUTHENTICATION + CONFIRM_CHANGE_EMAIL;
    protected static final String CHANGE_INFORMATION_URL = BASE_PATH_AUTHENTICATION + CHANGE_INFORMATION;
    protected static final String CHANGE_PASSWORD_URL = BASE_PATH_AUTHENTICATION + CHANGE_PASSWORD;
    protected static final String DELETE_ACCOUNT_URL = BASE_PATH_AUTHENTICATION + DELETE_ACCOUNT;
    protected static final String CONFIRM_EMAIL_URL = BASE_PATH_AUTHENTICATION + CONFIRM_EMAIL;
    protected static final String CHANGE_EMAIL_URL = BASE_PATH_AUTHENTICATION + CHANGE_EMAIL;
    protected static final String CHECK_EMAIL_URL = BASE_PATH_AUTHENTICATION + CHECK_EMAIL;
    protected static final String SEND_HINT_URL = BASE_PATH_AUTHENTICATION + SEND_HINT;
    protected static final String SIGN_UP_URL = BASE_PATH_AUTHENTICATION + SIGN_UP;
    protected static final String LOG_IN_URL = BASE_PATH_AUTHENTICATION + LOG_IN;

    protected static final String AUTH_HEADER_NAME = "Authorization";
    protected static final String AUTH_HEADER_PREFIX = "Bearer ";

    protected static final String EMAIL = "test@test.com";
    protected static final String PASSWORD = "password";
    protected static final String IP_ADDRESS = "1.1.1.1";
    protected static final String CODE = "code";

    protected static final String EN = "EN";

    protected static final String MESSAGE = "$.message";
}
