package com.ilmatty98.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilmatty98.constants.EmailTypeEnum;
import com.ilmatty98.dto.EmailTemplateDto;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static com.ilmatty98.constants.EmailTypeEnum.EmailConstants.DEFAULT_LANGUAGE;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EmailService {

    private final Mailer mailer;

    private final ObjectMapper objectMapper;

    /**
     * "\\$\\{[^}]+}" --> ${variableName}
     **/
    private static final String REGEX = "\\$\\{[^}]+}";

    public void sendEmail(String email, String language, EmailTypeEnum emailTypeEnum, Map<String, String> dynamicLabels) {
        try {
            log.warn("Init sending email to {}", email);

            var labelsInputStream = getResourceAsStreamOrThrow(emailTypeEnum.getLabelLocation());
            var labels = objectMapper.readValue(labelsInputStream, EmailTemplateDto.class);
            dynamicLabels.forEach((k, v) -> labels.getTemplate().put(k, Collections.singletonMap(DEFAULT_LANGUAGE, v)));

            var templateInputStream = getResourceAsStreamOrThrow(emailTypeEnum.getTemplateLocation());
            var template = new String(templateInputStream.readAllBytes(), StandardCharsets.UTF_8);

            var subject = getValue(labels.getSubject(), language);
            var body = fillTemplate(labels.getTemplate(), template, language);

            mailer.send(Mail.withHtml(email, subject, body));
            log.warn("Email sent successfully to {}", email);
        } catch (Exception e) {
            log.warn("Error sending email to {}", email, e);
        }
    }

    private static String getValue(Map<String, String> map, String language) {
        try {
            return map.getOrDefault(language, map.getOrDefault(DEFAULT_LANGUAGE, ""));
        } catch (Exception e) {
            return "";
        }
    }

    private static String fillTemplate(Map<String, Map<String, String>> labelsMap, String template, String language) {
        try {
            var substrings = extractSubstrings(template);
            for (var s : substrings) {
                var key = s.substring(2, s.length() - 1);
                template = template.replace(s, getValue(labelsMap.get(key), language));
            }
            return Optional.ofNullable(template).orElse("");
        } catch (Exception e) {
            return template;
        }
    }

    private static List<String> extractSubstrings(String input) {
        try {
            var matcher = Pattern.compile(REGEX).matcher(input);
            var substrings = new ArrayList<String>();
            while (matcher.find()) {
                substrings.add(matcher.group());
            }
            return substrings;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private InputStream getResourceAsStreamOrThrow(String resourcePath) {
        return Optional.ofNullable(ClassLoader.getSystemResourceAsStream(resourcePath))
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourcePath));
    }

}
