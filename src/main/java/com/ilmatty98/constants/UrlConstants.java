package com.ilmatty98.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("java:S1075")
public class UrlConstants {

    public static class Authentication {
        public static final String BASE_PATH_AUTHENTICATION = "/v1/authentication";

        public static final String LOG_IN = "/logIn";
        public static final String SIGN_UP = "/signUp";
        public static final String SEND_HINT = "/sendHint/{email}";
        public static final String CHECK_EMAIL = "/checkEmail/{email}";
        public static final String CHANGE_EMAIL = "/changeEmail";
        public static final String DELETE_ACCOUNT = "/deleteAccount";
        public static final String CHANGE_PASSWORD = "/changePassword";
        public static final String CHANGE_INFORMATION = "/changeInformation";
        public static final String CONFIRM_EMAIL = "/{email}/{code}/confirm";
        public static final String CONFIRM_CHANGE_EMAIL = "/confirmChangeEmail";
    }

    public static class Login {
        public static final String BASE_PATH_LOGIN = "/v1/login";
    }

    public static class Card {
        public static final String BASE_PATH_CARD = "/v1/card";
    }

}
