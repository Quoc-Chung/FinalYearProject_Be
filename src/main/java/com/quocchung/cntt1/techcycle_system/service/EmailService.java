package com.quocchung.cntt1.techcycle_system.service;

import java.util.Map;

public interface EmailService {
    void sendTemplateEmail(
        String to,
        String subject,
        String templateName,
        Map<String, Object> variables
    );
}
