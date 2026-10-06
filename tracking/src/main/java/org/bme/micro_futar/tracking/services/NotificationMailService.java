package org.bme.micro_futar.tracking.services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.tracking.entities.Notification;
import org.bme.micro_futar.tracking.enums.NotificationAudience;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NotificationMailService {

    private static final Locale LOCALE = Locale.forLanguageTag("hu-HU");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.mail.from}")
    private String fromAddress;
    @Value("${notification.mail.from-name}")
    private String fromName;
    @Value("${notification.tracking-page-url}")
    private String trackingPageUrl;

    private record MailContent(String heading, String template) {
    }

    public void send(Notification notification) throws MessagingException, UnsupportedEncodingException {
        MailContent content = contentFor(notification);
        String subject = content.heading() + " – " + notification.getParcelNumber();

        Context context = new Context(LOCALE);
        context.setVariable("subject", subject);
        context.setVariable("heading", content.heading());
        context.setVariable("name", notification.getToName());
        context.setVariable("parcelNumber", notification.getParcelNumber());
        context.setVariable("senderName", notification.getSenderName());
        context.setVariable("recipientName", notification.getRecipientName());
        context.setVariable("recipientAddress", notification.getRecipientAddress());
        context.setVariable("trackingUrl", trackingPageUrl + "?trackingNumber="
                + URLEncoder.encode(notification.getParcelNumber(), StandardCharsets.UTF_8));
        String html = templateEngine.process(content.template(), context);

        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
        helper.setFrom(fromAddress, fromName);
        helper.setTo(notification.getToEmail());
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private static MailContent contentFor(Notification notification) {
        return switch (notification.getType()) {
            case SHIPMENT_CREATED -> notification.getAudience() == NotificationAudience.SENDER
                    ? new MailContent("Sikeres csomagfeladás", "mail/shipment-created-sender")
                    : new MailContent("Csomagot adtak fel Önnek", "mail/shipment-created-recipient");
            case OUT_FOR_DELIVERY -> new MailContent("Csomagja úton van Önhöz", "mail/out-for-delivery");
            case DELIVERED -> new MailContent("Csomagja kézbesítve", "mail/delivered");
            case DELIVERY_FAILED -> new MailContent("Sikertelen kézbesítési kísérlet", "mail/delivery-failed");
        };
    }
}
