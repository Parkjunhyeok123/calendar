package com.company.calendar.ui.email;

import static kotlin.io.ByteStreamsKt.readBytes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class EmailComposeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_FILE = 1;

    private EditText editTo, editSubject, editBody;
    private TextView textFileName;
    private Uri fileUri = null;
    private String fileName = "첨부파일";

    private Gmail gmailService;
    private String accountEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_email_send);

        editTo = findViewById(R.id.editTextTo);
        editSubject = findViewById(R.id.editTextSubject);
        editBody = findViewById(R.id.editTextBody);
        textFileName = findViewById(R.id.textViewFileName);
        Button btnSelectFile = findViewById(R.id.btnSelectFile);
        Button btnSend = findViewById(R.id.btnSendEmail);

        // 계정 정보 받기
        accountEmail = getIntent().getStringExtra("accountEmail");

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this,
                java.util.List.of("https://www.googleapis.com/auth/gmail.send")
        );
        credential.setSelectedAccountName(accountEmail);

        gmailService = new Gmail.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Calendar Email App").build();

        // 첨부파일 선택
        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_FILE);
        });

        // 이메일 보내기
        btnSend.setOnClickListener(v -> {
            String to = editTo.getText().toString();
            String subject = editSubject.getText().toString();
            String body = editBody.getText().toString();

            if (to.isEmpty()) {
                Toast.makeText(this, "받는 사람 이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    MimeMessage email = createEmailWithOptionalAttachment(to, accountEmail, subject, body, fileUri);
                    Message message = createMessageWithEmail(email);
                    gmailService.users().messages().send("me", message).execute();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "이메일 전송 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "이메일 전송 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILE && resultCode == Activity.RESULT_OK && data != null) {
            fileUri = data.getData();
            fileName = getFileName(fileUri);
            textFileName.setText("선택된 파일: " + fileName);
        }
    }

    private String getFileName(Uri uri) {
        String result = "파일";
        try (var cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex);
                }
            }
        }
        return result;
    }

    private MimeMessage createEmailWithOptionalAttachment(String to, String from, String subject, String bodyText, @Nullable Uri fileUri)
            throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText);

        if (fileUri != null) {
            MimeBodyPart attachment = new MimeBodyPart();

            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] bytes = readBytes(inputStream);  // ← 안전한 방식 사용
            inputStream.close();

            DataSource dataSource = new ByteArrayDataSource(bytes, getContentResolver().getType(fileUri));
            attachment.setDataHandler(new DataHandler(dataSource));
            attachment.setFileName(fileName);

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachment);
            email.setContent(multipart);
        } else {
            email.setText(bodyText);
        }

        return email;
    }


    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
