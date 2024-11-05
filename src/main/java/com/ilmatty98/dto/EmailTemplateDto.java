package com.ilmatty98.dto;

import lombok.Data;

import java.util.Map;

@Data
public class EmailTemplateDto {

    private Map<String, String> subject;

    private Map<String, Map<String, String>> template;
}
