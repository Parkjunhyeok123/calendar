package com.company.calendar.ui.email;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import java.util.Collections;

public class GmailServiceHelper {
    public static Gmail getGmailService(Context context) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singletonList(GmailScopes.GMAIL_SEND)
        );
        credential.setSelectedAccountName(getUserEmail(context));

        return new Gmail.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("YourAppName").build();
    }

    public static String getUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return prefs.getString("email", ""); // 로그인 시 저장해둔 이메일
    }
}

