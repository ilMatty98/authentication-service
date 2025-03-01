package com.ilmatty98.mapper;

import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.authentication.request.SignUpDto;
import com.ilmatty98.dto.authentication.response.AccessDto;
import com.ilmatty98.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.UUID;

@Mapper(componentModel = "cdi")
public interface AuthenticationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attempt", ignore = true)
    @Mapping(target = "newEmail", ignore = true)
    @Mapping(target = "hint", source = "signUpDto.hint")
    @Mapping(target = "state", source = "accountStateEnum")
    @Mapping(target = "email", source = "signUpDto.email")
    @Mapping(target = "propic", source = "signUpDto.propic")
    @Mapping(target = "language", source = "signUpDto.language")
    @Mapping(target = "timestampEmail", source = "timestamp")
    @Mapping(target = "timestampPassword", source = "timestamp")
    @Mapping(target = "timestampCreation", source = "timestamp")
    @Mapping(target = "timestampLastAccess", source = "timestamp")
    @Mapping(target = "verificationCode", expression = "java(getUUID())")
    @Mapping(target = "salt", source = "salt", qualifiedByName = "base64Encoding")
    @Mapping(target = "hash", source = "hash", qualifiedByName = "base64Encoding")
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "credentials", ignore = true)
    Account newAccount(SignUpDto signUpDto, byte[] salt, byte[] hash, Timestamp timestamp, AccountStateEnum accountStateEnum);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "hint", source = "account.hint")
    @Mapping(target = "propic", source = "account.propic")
    @Mapping(target = "tokenPublicKey", source = "tokenPublicKey")
    @Mapping(target = "timestampEmail", source = "account.timestampEmail")
    @Mapping(target = "timestampPassword", source = "account.timestampPassword")
    @Mapping(target = "timestampCreation", source = "account.timestampCreation")
    @Mapping(target = "timestampLastAccess", source = "account.timestampLastAccess")
    AccessDto newAccessDto(Account account, String token, String tokenPublicKey);

    @Named("base64Encoding")
    default String base64Encoding(byte[] input) {
        return input != null ? Base64.getEncoder().encodeToString(input) : null;
    }

    @Named("base64Decoding")
    default byte[] base64Decoding(String input) {
        return input != null ? Base64.getDecoder().decode(input) : null;
    }

    @Named("base64EncodingString")
    default String base64EncodingString(String input) {
        return input != null ? base64Encoding(input.getBytes()) : null;
    }

    @Named("base64DecodingString")
    default String base64DecodingString(String input) {
        return input != null ? new String(base64Decoding(input)) : null;
    }

    @Named("getUUID")
    default String getUUID() {
        return UUID.randomUUID().toString();
    }


}
