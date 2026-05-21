package com.quocchung.cntt1.techcycle_system.service.impl;

import com.quocchung.cntt1.techcycle_system.service.EmailService;
import com.quocchung.cntt1.techcycle_system.utils.properties.EmailProperties;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
  private final SpringTemplateEngine templateEngine;
  private final JavaMailSender mailSender;
  private final EmailProperties emailProporties;


  @Override
  @Async("emailTaskExecutor")
  public void sendTemplateEmail(String to, String subject, String templateName,
      Map<String, Object> variables) {
    try {
      var context = new org.thymeleaf.context.Context();
      context.setVariables(variables);

      String htmlContent = templateEngine.process(templateName, context);

      sendMimeMessage(to, subject, htmlContent, true, null);

    } catch (Exception e) {
      log.error("Failed to send template email to {}", to, e);
    }
  }
  private void sendMimeMessage(String to,
      String subject,
      String content,
      boolean isHtml,
      List<File> attachments) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper =
          new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setFrom(emailProporties.getUsername());
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, isHtml);
      // Add attachments
      if (attachments != null && !attachments.isEmpty()) {
        for (File file : attachments) {
          helper.addAttachment(file.getName(), file);
        }
      }
      mailSender.send(mimeMessage);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send email", e);
    }
  }
}
