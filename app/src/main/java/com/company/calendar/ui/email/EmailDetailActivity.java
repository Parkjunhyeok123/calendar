package com.company.calendar.ui.email;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EmailDetailActivity extends AppCompatActivity {

    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_FILE = 1002;
    private static final int REQ_CODE_DIALOG_ATTACHMENT = 2001;
    private static final int REQ_CODE_ATTACHMENT = 2002;

    private GoogleAccountCredential credential;
    private Gmail gmailService;

    private TextView subjectView, fromView, dateView, bodyView;
    private Button replyButton, attachFileButton;

    private String accountEmail;
    private String messageId;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final List<File> selectedFiles = new ArrayList<>();
    private FileListReceiver tempFileListReceiver;

    interface FileListReceiver {
        void onFilesSelected(List<File> files);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_detail);

        subjectView = findViewById(R.id.detail_subject);
        fromView = findViewById(R.id.detail_from);
        dateView = findViewById(R.id.detail_date);
        bodyView = findViewById(R.id.detail_body);
        replyButton = findViewById(R.id.btn_reply);
        attachFileButton = findViewById(R.id.btn_attach_file);

        accountEmail = getIntent().getStringExtra("accountEmail");
        messageId = getIntent().getStringExtra("messageId");

        credential = GoogleAccountCredential.usingOAuth2(
                this,
                List.of("https://www.googleapis.com/auth/gmail.send", "https://www.googleapis.com/auth/gmail.readonly")
        );
        credential.setSelectedAccountName(accountEmail);

        gmailService = new Gmail.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Calendar Email App").build();

        subjectView.setText(getIntent().getStringExtra("subject"));
        fromView.setText(getIntent().getStringExtra("from"));
        dateView.setText(getIntent().getStringExtra("date"));

        replyButton.setOnClickListener(v -> showReplyDialog(
                getIntent().getStringExtra("from"),
                getIntent().getStringExtra("subject")
        ));
        attachFileButton.setOnClickListener(v -> openFilePicker());

        loadEmailBody();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "파일 선택"), REQUEST_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_FILE || requestCode == REQ_CODE_DIALOG_ATTACHMENT || requestCode == REQ_CODE_ATTACHMENT)
                && resultCode == RESULT_OK && data != null) {

            List<File> selected = new ArrayList<>();

            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    File file = FileUtils.getFileFromUri(this, uri);
                    if (file != null) selected.add(file);
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                File file = FileUtils.getFileFromUri(this, uri);
                if (file != null) selected.add(file);
            }

            if (requestCode == REQ_CODE_DIALOG_ATTACHMENT && tempFileListReceiver != null) {
                tempFileListReceiver.onFilesSelected(selected);
                tempFileListReceiver = null;
            } else {
                selectedFiles.clear();
                selectedFiles.addAll(selected);
                Toast.makeText(this, selected.size() + "개 파일 첨부됨", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            loadEmailBody();
        }
    }

    private void loadEmailBody() {
        executor.execute(() -> {
            try {
                Message message = gmailService.users().messages().get("me", messageId).setFormat("full").execute();
                String body = extractBody(message);
                mainHandler.post(() -> bodyView.setText(body));
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(this, "본문 불러오기 실패", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showReplyDialog(String to, String originalSubject) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reply_email, null);
        EditText editTextReply = dialogView.findViewById(R.id.editTextReply);
        Button buttonAttachFile = dialogView.findViewById(R.id.buttonAttachFile);
        TextView textViewSelectedFiles = dialogView.findViewById(R.id.textViewSelectedFiles);

        List<File> dialogSelectedFiles = new ArrayList<>();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("답장 메일 보내기")
                .setView(dialogView)
                .setPositiveButton("보내기", (d, which) -> {
                    String replyText = editTextReply.getText().toString();
                    sendReplyEmail(to, "Re: " + originalSubject, replyText, dialogSelectedFiles);
                })
                .setNegativeButton("취소", null)
                .create();

        buttonAttachFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "파일 선택"), REQ_CODE_DIALOG_ATTACHMENT);

            tempFileListReceiver = files -> {
                dialogSelectedFiles.clear();
                dialogSelectedFiles.addAll(files);
                String names = files.stream().map(File::getName).collect(Collectors.joining(", "));
                textViewSelectedFiles.setText("첨부됨: " + names);
            };
        });

        dialog.show();
    }

    private void sendReplyEmail(String to, String subject, String bodyText, List<File> attachments) {
        executor.execute(() -> {
            try {
                GmailSender sender = new GmailSender(gmailService, accountEmail);
                sender.sendEmail(to, subject, bodyText, attachments);

                mainHandler.post(() -> Toast.makeText(this, "답장 전송 완료!", Toast.LENGTH_SHORT).show());
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(this, "전송 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private String extractBody(Message message) {
        if (message == null || message.getPayload() == null) return "";

        if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
            byte[] data = Base64.decode(message.getPayload().getBody().getData(), Base64.URL_SAFE);
            return new String(data, StandardCharsets.UTF_8);
        } else if (message.getPayload().getParts() != null) {
            return getBodyFromParts(message.getPayload().getParts());
        }
        return "";
    }

    private String getBodyFromParts(List<MessagePart> parts) {
        for (MessagePart part : parts) {
            if ("text/plain".equals(part.getMimeType()) && part.getBody() != null) {
                byte[] data = Base64.decode(part.getBody().getData(), Base64.URL_SAFE);
                return new String(data, StandardCharsets.UTF_8);
            } else if (part.getParts() != null) {
                String result = getBodyFromParts(part.getParts());
                if (!result.isEmpty()) return result;
            }
        }
        return "";
    }
}
