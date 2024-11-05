package com.ilmatty98.dto.response;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class AccessDto {

    private String token;

    private String tokenPublicKey;

    private String protectedSymmetricKey;

    private String initializationVector;

    private String language;

    private String propic;

    private String hint;

    private Timestamp timestampCreation;

    private Timestamp timestampLastAccess;

    private Timestamp timestampPassword;

    private Timestamp timestampEmail;
}
