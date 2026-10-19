package com.bella.testapp;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class TitleBarJavaTestActivity extends AppCompatActivity {

    private View root;
    private View captionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_bar_test);

        root = findViewById(R.id.root);
        captionBar = findViewById(R.id.caption_bar);

        setupTransparentCaptionBar();
        setupCaptionBarInsets();
        setupGestureExclusion();


    }

    private void setupTransparentCaptionBar() {
        WindowInsetsController controller = getWindow().getInsetsController();

        if (controller != null) {
            controller.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND,
                    WindowInsetsController.APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND
            );
        }
    }

    private void setupCaptionBarInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(
                            View view,
                            WindowInsetsCompat windowInsets) {

                        Insets captionInsets =
                                windowInsets.getInsets(
                                        WindowInsetsCompat.Type.captionBar()
                                );

                        /*
                         * captionInsets.top
                         * captionInsets.bottom
                         * captionInsets.left
                         * captionInsets.right
                         */

                        // 自定义标题栏不需要额外 padding
                        captionBar.setPadding(
                                0,
                                0,
                                0,
                                0
                        );

                        return windowInsets;
                    }
                }
        );

        ViewCompat.requestApplyInsets(root);
    }

    private void setupGestureExclusion() {

        final View decorView = getWindow().getDecorView();

        Runnable updateGestureExclusion = new Runnable() {
            @Override
            public void run() {

                if (decorView.getWidth() <= 0) {
                    return;
                }

                int height = (int) (48 * getResources().getDisplayMetrics().density);

                List<Rect> rects = new ArrayList<>();

                Log.d("TitleBarTest","height "+height + ",width: "+decorView.getWidth());

                rects.add(new Rect(
                        decorView.getWidth()/2,
                        0,
                        decorView.getWidth(),
                        height
                ));

                decorView.setSystemGestureExclusionRects(rects);
            }
        };

        // 第一次更新
        updateGestureExclusion.run();

        // Layout 改变时更新
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        updateGestureExclusion.run();
                    }
                }
        );
    }


}