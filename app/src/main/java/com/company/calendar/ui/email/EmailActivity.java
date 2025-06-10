package com.company.calendar.ui.email;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.calendar.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailActivity extends AppCompatActivity {

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;

    private GoogleAccountCredential credential;
    private Gmail gmailService;

    private String selectedAccountEmail = null;  // 선택된 계정 이메일 저장 변수

    private RecyclerView emailList;
    private EmailAdapter emailAdapter;
    private List<EmailItem> emailItems = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        MaterialButton btnSendEmail = findViewById(R.id.btn_send_email);

        emailList = findViewById(R.id.email_list);
        emailList.setLayoutManager(new LinearLayoutManager(this));
        emailAdapter = new EmailAdapter(emailItems, new EmailAdapter.OnEmailClickListener() {
            @Override
            public void onEmailClick(EmailItem item) {
                Intent intent = new Intent(EmailActivity.this, EmailDetailActivity.class);
                intent.putExtra("subject", item.getSubject());
                intent.putExtra("from", item.getFrom());
                intent.putExtra("date", item.getDate());
                intent.putExtra("messageId", item.getMessageId());
                intent.putExtra("body", ""); // 본문은 필요시 다시 API 호출로 불러오기
                intent.putExtra("accountEmail", selectedAccountEmail);  // 여기에 추가
                startActivity(intent);
            }

        });
        emailList.setAdapter(emailAdapter);

        credential = GoogleAccountCredential.usingOAuth2(
                this,
                Arrays.asList(
                        GmailScopes.GMAIL_SEND,
                        GmailScopes.GMAIL_READONLY,
                        GmailScopes.GMAIL_LABELS
                )
        );

        chooseAccount();

        btnSendEmail.setOnClickListener(view ->
                sendEmail("example@gmail.com", "Subject Here", "Body Here"));
    }

    private void chooseAccount() {
        credential.setSelectedAccount(null);
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
                selectedAccountEmail = accountName;  // 선택한 계정 저장

                gmailService = new Gmail.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        GsonFactory.getDefaultInstance(),
                        credential
                ).setApplicationName("Calendar Email App").build();

                Log.d("EmailActivity", "선택된 계정: " + accountName);

                loadEmails();
            }
        } else if (requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == Activity.RESULT_OK) {
                loadEmails();
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadEmails() {
        if (selectedAccountEmail == null) {
            Toast.makeText(this, "계정을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                ListMessagesResponse response = gmailService.users().messages()
                        .list(selectedAccountEmail)
                        .setMaxResults(10L)
                        .execute();

                List<EmailItem> resultItems = new ArrayList<>();
                if (response.getMessages() != null) {
                    for (Message m : response.getMessages()) {
                        Message fullMessage = gmailService.users().messages()
                                .get(selectedAccountEmail, m.getId())
                                .setFormat("full")
                                .execute();

                        String subject = "", from = "", date = "", body = "";

                        for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
                            switch (header.getName()) {
                                case "Subject":
                                    subject = header.getValue();
                                    break;
                                case "From":
                                    from = header.getValue();
                                    break;
                                case "Date":
                                    date = header.getValue();
                                    break;
                            }
                        }

                        if (fullMessage.getPayload().getParts() != null) {
                            for (var part : fullMessage.getPayload().getParts()) {
                                if ("text/plain".equals(part.getMimeType())
                                        && part.getBody() != null && part.getBody().getData() != null) {
                                    body = new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
                                    break;
                                }
                            }
                        } else if (fullMessage.getPayload().getBody() != null
                                && fullMessage.getPayload().getBody().getData() != null) {
                            body = new String(Base64.getUrlDecoder().decode(fullMessage.getPayload().getBody().getData()));
                        }

                        resultItems.add(new EmailItem(subject, from, date, fullMessage.getId(), body));
                    }
                }

                mainHandler.post(() -> {
                    emailItems.clear();
                    emailItems.addAll(resultItems);
                    emailAdapter.notifyDataSetChanged();
                });

            } catch (UserRecoverableAuthIOException e) {
                Intent recoverIntent = e.getIntent();
                mainHandler.post(() -> startActivityForResult(recoverIntent, REQUEST_AUTHORIZATION));
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(this, "이메일을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void sendEmail(String to, String subject, String bodyText) {
        if (selectedAccountEmail == null) {
            Toast.makeText(this, "계정을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(EmailActivity.this, EmailComposeActivity.class);
        intent.putExtra("accountEmail", selectedAccountEmail);
        startActivityForResult(intent, 3000);
    }




    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
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