package vn.iotstar.ktqt03.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import vn.iotstar.ktqt03.util.Jpa;

public class MailService {
    public void sendOtp(String to, String code) throws MessagingException {
        String user = Jpa.requiredEnv("KTQT_SMTP_USER");
        String host = Jpa.requiredEnv("KTQT_SMTP_HOST");
        boolean local = host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost");
        boolean auth = !"false".equalsIgnoreCase(System.getenv("KTQT_SMTP_AUTH"));
        boolean tls = !"false".equalsIgnoreCase(System.getenv("KTQT_SMTP_STARTTLS"));
        if ((!auth || !tls) && !local)
            throw new IllegalStateException("SMTP không xác thực/TLS chỉ được dùng với máy chủ kiểm thử localhost.");
        String pass = auth ? Jpa.requiredEnv("KTQT_SMTP_PASSWORD") : "";
        Properties p = new Properties();
        p.setProperty("mail.smtp.host", host);
        p.setProperty("mail.smtp.port", Jpa.requiredEnv("KTQT_SMTP_PORT"));
        p.setProperty("mail.smtp.auth", Boolean.toString(auth));
        p.setProperty("mail.smtp.starttls.enable", Boolean.toString(tls));
        p.setProperty("mail.smtp.starttls.required", Boolean.toString(tls));
        p.setProperty("mail.smtp.connectiontimeout", "10000");
        p.setProperty("mail.smtp.timeout", "10000");
        p.setProperty("mail.smtp.writetimeout", "10000");

        Session mail = Session.getInstance(p, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        MimeMessage message = new MimeMessage(mail);
        message.setFrom(new InternetAddress(user));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, true));
        message.setSubject("Mã kích hoạt tài khoản — Đề 03", "UTF-8");
        message.setText("Mã OTP của bạn: " + code + "\nCó hiệu lực trong 5 phút.", "UTF-8");
        Transport.send(message);
    }
}
