package com.company.calendar.ui.card;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.company.calendar.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CardEditorActivity extends AppCompatActivity {

    private FrameLayout cardLayout;
    private TextView movableText;
    private Spinner spinnerFont, spinnerTheme;
    private Button btnColor, btnImage, btnSave, btnChangeBg, btnAddText;
    private SeekBar seekSize;
    private EditText editTextInput;

    private int currentColor = Color.BLACK;
    private Typeface currentFont;
    private Uri selectedImageUri;
    private TextView selectedTextView = null;

    private ActivityResultLauncher<String> imagePickerLauncher;

    // 이미지 아이콘 관련 변수
    private ImageView selectedImageView = null;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // 앱 내 아이콘 리소스 배열 (예시로 3개 아이콘)
    private int[] iconResIds = {
            R.drawable.ic_kakao,
            R.drawable.ic_kakao,
            R.drawable.ic_kakao
    };
    private String[] iconNames = {
            "아이콘 1",
            "아이콘 2",
            "아이콘 3"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_editor);

        cardLayout = findViewById(R.id.cardLayout);
        movableText = findViewById(R.id.movableText);
        spinnerFont = findViewById(R.id.spinnerFont);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        btnColor = findViewById(R.id.buttonColor);
        btnImage = findViewById(R.id.buttonImage);
        btnSave = findViewById(R.id.buttonSave);
        btnChangeBg = findViewById(R.id.buttonChangeBg);
        btnAddText = findViewById(R.id.buttonAddText);
        editTextInput = findViewById(R.id.editTextInput);
        seekSize = findViewById(R.id.seekSize);

        // 폰트 스피너 설정
        ArrayAdapter<CharSequence> fontAdapter = ArrayAdapter.createFromResource(this,
                R.array.font_list, android.R.layout.simple_spinner_item);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(fontAdapter);

        spinnerFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fontName = parent.getItemAtPosition(position).toString();
                currentFont = Typeface.create(fontName, Typeface.NORMAL);
                if (selectedTextView != null) {
                    selectedTextView.setTypeface(currentFont);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 테마 설정
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(this,
                R.array.theme_list, android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);

        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: cardLayout.setBackgroundColor(Color.WHITE); break;
                    case 1: cardLayout.setBackgroundColor(Color.LTGRAY); break;
                    case 2: cardLayout.setBackgroundColor(Color.parseColor("#FFE4B5")); break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnColor.setOnClickListener(v -> {
            if (selectedTextView == null) {
                Toast.makeText(this, "변경할 텍스트를 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentColor = (currentColor == Color.BLACK) ? Color.BLUE : Color.BLACK;
            selectedTextView.setTextColor(currentColor);
        });

        seekSize.setMax(60);
        seekSize.setProgress(24);
        seekSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (selectedTextView != null) {
                    selectedTextView.setTextSize(progress);
                }
                else if (selectedImageView != null) {
                    float scale = progress / 100f;
                    scale = Math.max(0.3f, Math.min(scale, 10.0f));  // 최소~최대 스케일 제한
                    selectedImageView.setScaleX(scale);
                    selectedImageView.setScaleY(scale);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            cardLayout.setBackground(new BitmapDrawable(getResources(), inputStream));
                            inputStream.close();
                        } catch (Exception e) {
                            Toast.makeText(this, "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        btnChangeBg.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnAddText.setOnClickListener(v -> {
            String userText = editTextInput.getText().toString().trim();
            if (!userText.isEmpty()) {
                TextView newTextView = new TextView(this);
                newTextView.setText(userText);
                newTextView.setTypeface(currentFont);
                newTextView.setTextColor(currentColor);
                newTextView.setTextSize(seekSize.getProgress());
                newTextView.setX(50f);
                newTextView.setY(50f);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                newTextView.setLayoutParams(params);

                newTextView.setOnTouchListener(textTouchListener);

                cardLayout.addView(newTextView);
                editTextInput.setText("");
                selectedTextView = newTextView;
                selectedImageView = null; // 텍스트 선택 상태
                highlightSelectedTextView();
                removeHighlightImageView();
            } else {
                Toast.makeText(this, "텍스트를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        btnImage.setOnClickListener(v -> showIconPickerDialog());

        btnSave.setOnClickListener(v -> saveCardAsImage());

        currentFont = Typeface.DEFAULT;
        updatePreview();

        // movableText에도 터치 리스너 적용 (이동 가능)
        movableText.setOnTouchListener(textTouchListener);

        // 핀치 줌, 제스처 리스너 생성
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        gestureDetector = new GestureDetector(this, new GestureListener());
    }

    /** 아이콘 선택 다이얼로그 **/
    private void showIconPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("아이콘 선택");
        builder.setItems(iconNames, (dialog, which) -> {
            int resId = iconResIds[which];
            addIconToCard(resId);
        });
        builder.show();
    }

    /** 카드에 아이콘 추가 **/
    private void addIconToCard(int resId) {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(resId);
        imageView.setX(50f);
        imageView.setY(50f);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setMaxWidth(300);
        imageView.setMaxHeight(300);

        // 터치, 이동, 크기 조절, 롱프레스 삭제 리스너 설정
        imageView.setOnTouchListener(iconTouchListener);

        cardLayout.addView(imageView);

        // 선택 상태 변경
        selectedImageView = imageView;
        selectedTextView = null;
        highlightSelectedImageView();
        removeHighlight();
    }

    /** 텍스트 터치 리스너 (이동 및 삭제 롱프레스 3초) **/
    private final View.OnTouchListener textTouchListener = new View.OnTouchListener() {
        float dX, dY;
        long pressStartTime;
        Handler handler = new Handler();
        Runnable longPressRunnable;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressStartTime = System.currentTimeMillis();
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    selectedTextView = (TextView) v;
                    selectedImageView = null;
                    highlightSelectedTextView();
                    removeHighlightImageView();

                    // 롱프레스 3초 삭제 처리
                    handler.removeCallbacks(longPressRunnable);
                    longPressRunnable = () -> {
                        if (v == movableText) {
                            Toast.makeText(CardEditorActivity.this, "기본 텍스트는 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            cardLayout.removeView(v);
                            if (selectedTextView == v) {
                                selectedTextView = null;
                                removeHighlight();
                            }
                            Toast.makeText(CardEditorActivity.this, "텍스트가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    };
                    handler.postDelayed(longPressRunnable, 3000);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    v.setX(event.getRawX() + dX);
                    v.setY(event.getRawY() + dY);
                    return true;

                case MotionEvent.ACTION_UP:
                    handler.removeCallbacks(longPressRunnable);
                    return true;
            }
            return false;
        }
    };

    /** 아이콘 터치 리스너 (이동, 핀치 줌, 롱프레스 삭제) **/
    private final View.OnTouchListener iconTouchListener = new View.OnTouchListener() {
        float dX, dY;
        float scaleFactor = 1.0f;
        long pressStartTime;
        Handler handler = new Handler();
        Runnable longPressRunnable;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    pressStartTime = System.currentTimeMillis();
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();

                    selectedImageView = (ImageView) v;
                    selectedTextView = null;
                    highlightSelectedImageView();
                    removeHighlightTextView();

                    handler.removeCallbacks(longPressRunnable);
                    longPressRunnable = () -> {
                        cardLayout.removeView(v);
                        if (selectedImageView == v) {
                            selectedImageView = null;
                            removeHighlight();
                        }
                        Toast.makeText(CardEditorActivity.this, "아이콘이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    };
                    handler.postDelayed(longPressRunnable, 3000);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    v.setX(event.getRawX() + dX);
                    v.setY(event.getRawY() + dY);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(longPressRunnable);
                    return true;
            }
            return false;
        }
    };

    /** 핀치 줌 스케일 리스너 **/
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (selectedImageView != null) {
                float scale = detector.getScaleFactor();
                float newScaleX = selectedImageView.getScaleX() * scale;
                float newScaleY = selectedImageView.getScaleY() * scale;

                // 최소/최대 크기 제한
                newScaleX = Math.max(0.3f, Math.min(newScaleX, 3.0f));
                newScaleY = Math.max(0.3f, Math.min(newScaleY, 3.0f));

                selectedImageView.setScaleX(newScaleX);
                selectedImageView.setScaleY(newScaleY);
            }
            return true;
        }
    }

    /** 제스처 리스너 (필요 시 확장 가능) **/
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // 이미지나 텍스트 선택 해제 등 구현 가능
            return super.onSingleTapConfirmed(e);
        }
    }

    /** 텍스트 선택 강조 표시 **/
    private void highlightSelectedTextView() {
        for (int i = 0; i < cardLayout.getChildCount(); i++) {
            View child = cardLayout.getChildAt(i);
            if (child instanceof TextView) {
                if (child == selectedTextView) {
                    child.setBackgroundColor(Color.parseColor("#8033B5E5")); // 반투명 강조색
                } else {
                    child.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }

    private void removeHighlightTextView() {
        for (int i = 0; i < cardLayout.getChildCount(); i++) {
            View child = cardLayout.getChildAt(i);
            if (child instanceof TextView) {
                child.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    /** 아이콘 선택 강조 표시 **/
    private void highlightSelectedImageView() {
        for (int i = 0; i < cardLayout.getChildCount(); i++) {
            View child = cardLayout.getChildAt(i);
            if (child instanceof ImageView) {
                if (child == selectedImageView) {
                    child.setBackgroundColor(Color.parseColor("#8033B5E5"));
                } else {
                    child.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }

    private void removeHighlightImageView() {
        for (int i = 0; i < cardLayout.getChildCount(); i++) {
            View child = cardLayout.getChildAt(i);
            if (child instanceof ImageView) {
                child.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    /** 모든 선택 상태 해제 **/
    private void removeHighlight() {
        removeHighlightTextView();
        removeHighlightImageView();
    }

    /** 카드 이미지 저장 (기존 로직) **/
    private void saveCardAsImage() {
        // 카드뷰 크기만큼 Bitmap 생성
        Bitmap bitmap = Bitmap.createBitmap(cardLayout.getWidth(), cardLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        cardLayout.draw(canvas);

        try {
            String filename = "card_" + System.currentTimeMillis() + ".png";
            OutputStream fos;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
                File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File image = new File(imagesDir, filename);
                fos = new FileOutputStream(image);
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "카드 이미지 저장 완료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreview() {
        movableText.setTypeface(currentFont);
        movableText.setTextColor(currentColor);
        movableText.setTextSize(seekSize.getProgress());
    }
}
