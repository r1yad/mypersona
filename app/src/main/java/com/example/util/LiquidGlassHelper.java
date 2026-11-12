package com.example.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Centralized Liquid Glass UI helper for edge-to-edge system bar insets,
 * smooth page transitions, reduced-motion accessibility compliance,
 * tactile spring press feedback, hardware-accelerated modal window blur (API 31+),
 * and unified confirmation dialogs.
 */
public final class LiquidGlassHelper {

    private static final long NAV_DEBOUNCE_MS = 320L;
    private static long lastNavigationTimeMs = 0L;

    private LiquidGlassHelper() {
    }

    /**
     * Enables full edge-to-edge drawing with light icons/text over our dark obsidian canvas.
     */
    public static void enableEdgeToEdge(@NonNull Activity activity) {
        Window window = activity.getWindow();
        if (window == null) return;
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }
    }

    /**
     * Adds status bar (plus display cutout) top inset to the target view while preserving its original XML padding.
     */
    public static void applyTopInsets(@NonNull View view) {
        final int padLeft = view.getPaddingLeft();
        final int padTop = view.getPaddingTop();
        final int padRight = view.getPaddingRight();
        final int padBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(padLeft + bars.left, padTop + bars.top, padRight + bars.right, padBottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /**
     * Adds navigation bar bottom inset to the target view while preserving its original XML padding.
     */
    public static void applyBottomInsets(@NonNull View view) {
        final int padLeft = view.getPaddingLeft();
        final int padTop = view.getPaddingTop();
        final int padRight = view.getPaddingRight();
        final int padBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(padLeft + navBars.left, padTop, padRight + navBars.right, padBottom + navBars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /**
     * Adds navigation bar + IME keyboard bottom inset to scrollable forms or docked input bars
     * while preserving initial XML padding.
     */
    public static void applyImeBottomInsets(@NonNull View view) {
        final int padLeft = view.getPaddingLeft();
        final int padTop = view.getPaddingTop();
        final int padRight = view.getPaddingRight();
        final int padBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets imeAndNav = windowInsets.getInsets(
                    WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(padLeft + imeAndNav.left, padTop, padRight + imeAndNav.right,
                    padBottom + imeAndNav.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /**
     * Applies top status bar inset to topChrome and bottom navigation/IME inset to bottomContainer.
     */
    public static void applyScreenInsets(@Nullable View topChrome, @Nullable View bottomContainer, boolean includeIme) {
        if (topChrome != null) {
            applyTopInsets(topChrome);
        }
        if (bottomContainer != null) {
            if (includeIme) {
                applyImeBottomInsets(bottomContainer);
            } else {
                applyBottomInsets(bottomContainer);
            }
        }
    }

    /**
     * Checks system animator duration scale so reduced-motion accessibility users are respected.
     */
    public static boolean isReducedMotionEnabled(@NonNull Context context) {
        try {
            float scale = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1.0f);
            return scale == 0.0f;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Debounces rapid consecutive taps to prevent duplicate activity transitions or stacked modals.
     */
    public static synchronized boolean canNavigate() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastNavigationTimeMs < NAV_DEBOUNCE_MS) {
            return false;
        }
        lastNavigationTimeMs = now;
        return true;
    }

    public static void startActivityWithGlassTransition(@NonNull Activity activity, @NonNull Intent intent) {
        if (!canNavigate()) return;
        activity.startActivity(intent);
        if (isReducedMotionEnabled(activity)) {
            activity.overridePendingTransition(0, 0);
        } else {
            activity.overridePendingTransition(R.anim.glass_enter, R.anim.glass_exit);
        }
    }

    public static void startActivityWithGlassTransition(@NonNull Fragment fragment, @NonNull Intent intent) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            startActivityWithGlassTransition(activity, intent);
        }
    }

    public static void finishWithGlassTransition(@NonNull Activity activity) {
        activity.finish();
        if (isReducedMotionEnabled(activity)) {
            activity.overridePendingTransition(0, 0);
        } else {
            activity.overridePendingTransition(R.anim.glass_pop_enter, R.anim.glass_pop_exit);
        }
    }

    /**
     * Applies crisp Liquid Glass tactile scale-down and spring-back feedback without stealing clicks.
     * Note: Attach only to individual interactive buttons/chips, never an entire post card
     * when targeting child action buttons.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void attachPressFeedback(@NonNull View... views) {
        for (View view : views) {
            if (view == null) continue;
            view.setOnTouchListener((v, event) -> {
                if (!v.isEnabled() || isReducedMotionEnabled(v.getContext())) {
                    return false;
                }
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    v.animate().cancel();
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .alpha(0.90f)
                            .setDuration(90L)
                            .start();
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    v.animate().cancel();
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .alpha(1.0f)
                            .setDuration(170L)
                            .setInterpolator(new OvershootInterpolator(1.5f))
                            .start();
                }
                return false;
            });
        }
    }

    /**
     * Brief spring pop animation for reaction toggles or tab icon confirmation.
     */
    public static void pulseView(@Nullable View view) {
        if (view == null || isReducedMotionEnabled(view.getContext())) return;
        view.animate().cancel();
        view.animate()
                .scaleX(1.14f)
                .scaleY(1.14f)
                .setDuration(95L)
                .withEndAction(() -> view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150L)
                        .setInterpolator(new OvershootInterpolator(1.8f))
                        .start())
                .start();
    }

    /**
     * Enables genuine cross-window background blur on API 31+ dialogs/sheets when supported by the device,
     * keeping translucent Liquid Glass fallback intact on older APIs.
     */
    public static void enableDialogWindowBlur(@Nullable Window window) {
        if (window == null) return;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.58f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                WindowManager wm = window.getWindowManager();
                if (wm != null && wm.isCrossWindowBlurEnabled()) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.setBlurBehindRadius(36);
                    window.setAttributes(lp);
                    window.setBackgroundBlurRadius(32);
                }
            } catch (Throwable ignored) {
                // Fallback gracefully to static frosted glass drawable
            }
        }
    }

    /**
     * Displays a unified frosted Liquid Glass confirmation dialog across the entire app.
     */
    @NonNull
    public static AlertDialog showGlassConfirmDialog(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String message,
            @NonNull String positiveLabel,
            boolean isDestructive,
            @NonNull Runnable onConfirm) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(context, R.style.Theme_PersonaNet_AlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(positiveLabel, (d, which) -> onConfirm.run())
                .create();
        dialog.show();
        enableDialogWindowBlur(dialog.getWindow());

        if (isDestructive) {
            android.widget.Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveBtn != null) {
                positiveBtn.setTextColor(ContextCompat.getColor(context, R.color.red_error));
            }
        }
        return dialog;
    }

    /**
     * Lightweight 60 FPS ViewPager2 PageTransformer for smooth depth-scale & fade image slider transitions.
     */
    @NonNull
    public static ViewPager2.PageTransformer createSmoothSliderTransformer() {
        return (page, position) -> {
            float absPos = Math.abs(position);
            if (absPos >= 1f) {
                page.setAlpha(0.85f);
                page.setScaleX(0.95f);
                page.setScaleY(0.95f);
            } else {
                float scale = 0.95f + (1f - absPos) * 0.05f;
                float alpha = 0.85f + (1f - absPos) * 0.15f;
                page.setScaleX(scale);
                page.setScaleY(scale);
                page.setAlpha(alpha);
            }
        };
    }
}
