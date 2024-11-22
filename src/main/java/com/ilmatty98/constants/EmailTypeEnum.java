package com.ilmatty98.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;


@Getter
@AllArgsConstructor
public enum EmailTypeEnum {

    LOG_IN(EmailConstants.BASE_PATH_LABEL + "logIn.json", EmailConstants.BASE_PATH_TEMPLATE + "logIn.html"),
    SING_UP(EmailConstants.BASE_PATH_LABEL + "signUp.json", EmailConstants.BASE_PATH_TEMPLATE + "signUp.html"),
    SEND_HINT(EmailConstants.BASE_PATH_LABEL + "sendHint.json", EmailConstants.BASE_PATH_TEMPLATE + "sendHint.html"),
    CHANGE_PSW(EmailConstants.BASE_PATH_LABEL + "changePsw.json", EmailConstants.BASE_PATH_TEMPLATE + "changePsw.html"),
    DELETE_USER(EmailConstants.BASE_PATH_LABEL + "deleteUser.json", EmailConstants.BASE_PATH_TEMPLATE + "deleteUser.html"),
    CHANGE_EMAIL(EmailConstants.BASE_PATH_LABEL + "changeEmail.json", EmailConstants.BASE_PATH_TEMPLATE + "changeEmail.html"),
    CHANGE_EMAIL_CODE(EmailConstants.BASE_PATH_LABEL + "changeEmailCode.json", EmailConstants.BASE_PATH_TEMPLATE + "changeEmailCode.html"),
    CHANGE_EMAIL_NOTIFICATION(EmailConstants.BASE_PATH_LABEL + "changeEmailNotification.json", EmailConstants.BASE_PATH_TEMPLATE + "changeEmailNotification.html");

    private final String labelLocation;
    private final String templateLocation;

    @UtilityClass
    public static class EmailConstants {

        private static final String BASE_PATH = "email/";
        private static final String BASE_PATH_LABEL = BASE_PATH + "label/";
        private static final String BASE_PATH_TEMPLATE = BASE_PATH + "template/";

        public static final String DEFAULT_LANGUAGE = "EN";
    }
}
