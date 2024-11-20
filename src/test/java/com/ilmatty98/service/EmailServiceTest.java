package com.ilmatty98.service;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.EmailTypeEnum;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EmailServiceTest extends AuthenticationServiceTests {

    private static final String EMAIL_TO = "test@test.com";
    private static final String EN = "EN";
    private static final String IT = "IT";

    @Test
    void testIT() throws MessagingException {
        verifyLanguage(IT, "Nuovo accesso su Credential Manager!", "Data:");
    }

    @Test
    void testEN() throws MessagingException {
        verifyLanguage(EN, "New access on Credential Manager!", "Date:");
    }

    @Test
    void testDifferentLanguage() throws MessagingException {
        verifyLanguage("HR", "New access on Credential Manager!", "Date:");
    }

    @Test
    void testLogIn() throws MessagingException {
        var expectedSubject = "New access on Credential Manager!";
        var label = List.of("Date", "IP Address", "Device Type", "Credentials Manager");
        var dynamicLabels = new HashMap<String, String>();
        dynamicLabels.put("date_value", LocalDateTime.now().toString());
        dynamicLabels.put("ipAddress_value", "255.255.255.255");
        dynamicLabels.put("device_value", "Chrome");

        verifyEmail(EmailTypeEnum.LOG_IN, expectedSubject, label, dynamicLabels);
    }

    @Test
    void testSignUp() throws MessagingException {
        var expectedSubject = "Welcome to Credentials Manager!";
        var dynamicLabels = new HashMap<String, String>();
        var label = List.of("Welcome to Credentials Manager!", "Click on this link to confirm the account", "Credentials Manager");
        dynamicLabels.put("href", generateRandomString(20) + 1);

        verifyEmail(EmailTypeEnum.SING_UP, expectedSubject, label, dynamicLabels);
    }

    @Test
    @DisabledOnOs(OS.LINUX)
    void testChangePsw() throws MessagingException {
        var expectedSubject = "Password changed!";
        var label = List.of("Password changed!", "Password has been changed!", "Credentials Manager");

        verifyEmail(EmailTypeEnum.CHANGE_PSW, expectedSubject, label, new HashMap<>());
    }

    @Test
    void testChangeEmail() throws MessagingException {
        var expectedSubject = "Email changed!";
        var label = List.of("Email changed!", "Email has been changed!", "Credentials Manager");

        verifyEmail(EmailTypeEnum.CHANGE_EMAIL, expectedSubject, label, new HashMap<>());
    }

    @Test
    void testChangeEmailCode() throws MessagingException {
        var expectedSubject = "Your Email Change!";
        var label = List.of("Your Email Change!", "To finalize changing your Bitwarden email address enter the following code in web vault", "Credentials Manager");
        var dynamicLabels = new HashMap<String, String>();
        dynamicLabels.put("code", "123456");

        verifyEmail(EmailTypeEnum.CHANGE_EMAIL_CODE, expectedSubject, label, dynamicLabels);
    }

    @Test
    void testChangeEmailNotification() throws MessagingException {
        var expectedSubject = "Your Email Change!";
        var label = List.of("Your Email Change!", "A change to your account was recently attempted to use this new email address", "Credentials Manager");
        var dynamicLabels = new HashMap<String, String>();
        dynamicLabels.put("email", "test@test.com");

        verifyEmail(EmailTypeEnum.CHANGE_EMAIL_NOTIFICATION, expectedSubject, label, dynamicLabels);
    }

    @Test
    void testSendHint() throws MessagingException {
        var expectedSubject = "Your Master Password Hint";
        var label = List.of("You (or someone) recently requested your master password hint.", "Your hint is", "Credentials Manager");
        var dynamicLabels = new HashMap<String, String>();
        dynamicLabels.put("hint_value", generateRandomString(20) + 1);

        verifyEmail(EmailTypeEnum.SEND_HINT, expectedSubject, label, dynamicLabels);
    }

    private void verifyLanguage(String language, String expectedSubject, String expectedContainsBody) throws MessagingException {
        emailService.sendEmail(EMAIL_TO, language, EmailTypeEnum.LOG_IN, new HashMap<>());

        var receivedMessages = greenMail.getReceivedMessages();
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals(1, receivedMessages.length);

        var email = receivedMessages[0];
        assertEquals(1, email.getAllRecipients().length);
        assertEquals(emailFrom, email.getFrom()[0].toString());
        assertEquals(EMAIL_TO, email.getAllRecipients()[0].toString());
        assertEquals(expectedSubject, email.getSubject());
        assertTrue(GreenMailUtil.getBody(email).contains(expectedContainsBody));
    }

    private void verifyEmail(EmailTypeEnum emailType, String expectedSubject, List<String> label,
                             Map<String, String> dynamicLabels) throws MessagingException {
        emailService.sendEmail(EMAIL_TO, EmailServiceTest.EN, emailType, dynamicLabels);

        var receivedMessages = greenMail.getReceivedMessages();
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals(1, receivedMessages.length);

        var email = receivedMessages[0];
        assertEquals(1, email.getAllRecipients().length);
        assertEquals(emailFrom, email.getFrom()[0].toString());
        assertEquals(EMAIL_TO, email.getAllRecipients()[0].toString());
        assertEquals(expectedSubject, email.getSubject());

        var body = GreenMailUtil.getBody(email)
                .replaceAll("=\r\n", "");

        var elements = Stream.concat(dynamicLabels.values().stream(), label.stream()).toList();
        assertTrue(elements.stream().allMatch(body::contains));
    }

}
