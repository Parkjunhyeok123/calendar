package com.company.calendar.ui.email;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

public class GmailSender {

    private Gmail gmailService;
    private String userEmail;

    public GmailSender(Gmail gmailService, String userEmail) {
        this.gmailService = gmailService;
        this.userEmail = userEmail;
    }

    /**
     * 첨부파일 포함 메일 보내기
     * @param to 받는 사람 이메일
     * @param subject 제목
     * @param bodyText 본문 텍스트
     * @param attachmentFiles 첨부파일 리스트 (File 객체)
     */
    public void sendEmail(String to, String subject, String bodyText, List<File> attachmentFiles) throws MessagingException, IOException {
        MimeMessage email = createEmailWithAttachments(to, userEmail, subject, bodyText, attachmentFiles);
        sendMessage(email);
    }

    // MIME 메시지 생성 (첨부파일 포함)
    private MimeMessage createEmailWithAttachments(String to, String from, String subject, String bodyText, List<File> attachmentFiles) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        // 본문 부분
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText, "utf-8");

        // 첨부파일 부분
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        if (attachmentFiles != null) {
            for (File file : attachmentFiles) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                FileDataSource source = new FileDataSource(file);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(file.getName());
                multipart.addBodyPart(attachmentPart);
            }
        }

        email.setContent(multipart);

        return email;
    }

    // Gmail API를 통해 메시지 전송
    private void sendMessage(MimeMessage email) throws IOException, MessagingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();

        // Base64 URL-safe 인코딩
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        gmailService.users().messages().send(userEmail, message).execute();
    }
}
