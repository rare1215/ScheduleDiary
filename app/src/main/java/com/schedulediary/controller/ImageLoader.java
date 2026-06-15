package com.schedulediary.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 사용자의 로컬 저장소로부터 이미지를 불러오고, 용량 체크·최적화를 담당하는 클래스 (Design.md §7)
 */
public class ImageLoader {

    /** 최대 허용 파일 크기: 15MB (Design.md §7 maxImageSize) */
    private static final long MAX_IMAGE_SIZE = 15_728_640L;

    /** 최적화 후 출력 품질 */
    private static final int COMPRESS_QUALITY = 85;

    /** 최적화 후 최대 가로/세로 크기 */
    private static final int MAX_DIMENSION = 1920;

    private final Context context;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface ImageCallback {
        void onSuccess(String optimizedPath);
        void onError(String message);
    }

    public ImageLoader(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ────────────────────────────────────────────────
    // 이미지 로드 파이프라인 (Design.md §7)
    // ────────────────────────────────────────────────

    /**
     * 갤러리에서 선택된 Uri를 받아 → 크기 검증 → 최적화 → 내부 저장소 복사 → 경로 반환
     */
    public void loadImage(Uri imageUri, ImageCallback callback) {
        executor.execute(() -> {
            try {
                // 1. 파일 크기 검증 (Design.md §7 validateImageSize)
                if (!validateImageSize(imageUri)) {
                    postError(callback, "이미지 크기가 15MB를 초과합니다.\n더 작은 이미지를 선택해주세요.");
                    return;
                }

                // 2. 최적화 (Design.md §7 optimizeImage)
                String optimizedPath = optimizeAndSave(imageUri);
                if (optimizedPath == null) {
                    postError(callback, "이미지를 불러오는 데 실패했습니다.");
                    return;
                }

                // 3. 경로 반환
                postSuccess(callback, optimizedPath);

            } catch (Exception e) {
                postError(callback, "이미지 처리 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }

    // ────────────────────────────────────────────────
    // 내부 구현
    // ────────────────────────────────────────────────

    /**
     * 파일이 15MB 이하인지 검증 (Design.md §7 validateImageSize)
     */
    private boolean validateImageSize(Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return false;
            long size = 0;
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) {
                size += read;
                if (size > MAX_IMAGE_SIZE) {
                    is.close();
                    return false;
                }
            }
            is.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 시스템에 무리가 가는 이미지를 경량화 (Design.md §7 optimizeImage)
     * → 앱 내부 저장소(cache/images/)에 저장 후 경로 반환
     */
    private String optimizeAndSave(Uri uri) {
        try {
            // 원본 읽기 (inJustDecodeBounds로 크기만 먼저 체크)
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            InputStream is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, opts);
            if (is != null) is.close();

            int origW = opts.outWidth;
            int origH = opts.outHeight;

            // 다운샘플링 비율 계산
            int sampleSize = 1;
            while (origW / sampleSize > MAX_DIMENSION || origH / sampleSize > MAX_DIMENSION) {
                sampleSize *= 2;
            }

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = sampleSize;
            opts.inPreferredConfig = Bitmap.Config.RGB_565; // 메모리 절약

            is = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
            if (is != null) is.close();

            if (bitmap == null) return null;

            // 내부 저장소에 저장
            File imagesDir = new File(context.getFilesDir(), "diary_images");
            if (!imagesDir.exists()) imagesDir.mkdirs();

            String filename = "img_" + System.currentTimeMillis() + ".jpg";
            File outFile = new File(imagesDir, filename);

            FileOutputStream fos = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();

            return outFile.getAbsolutePath();

        } catch (Exception e) {
            return null;
        }
    }

    private void postSuccess(ImageCallback callback, String path) {
        mainHandler.post(() -> callback.onSuccess(path));
    }

    private void postError(ImageCallback callback, String msg) {
        mainHandler.post(() -> callback.onError(msg));
    }

    // ────────────────────────────────────────────────
    // 배경 이미지 크롭 (5번 수정사항)
    // ────────────────────────────────────────────────

    public interface BitmapCallback {
        void onSuccess(Bitmap bitmap);
        void onError(String message);
    }

    /**
     * 크롭 다이얼로그에 표시할 원본 이미지를 적당한 해상도로 로드 (크기 검증 포함)
     */
    public void loadBitmapForCrop(Uri imageUri, BitmapCallback callback) {
        executor.execute(() -> {
            try {
                if (!validateImageSize(imageUri)) {
                    postBitmapError(callback, "이미지 크기가 15MB를 초과합니다.\n더 작은 이미지를 선택해주세요.");
                    return;
                }

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                InputStream is = context.getContentResolver().openInputStream(imageUri);
                BitmapFactory.decodeStream(is, null, opts);
                if (is != null) is.close();

                int sampleSize = 1;
                while (opts.outWidth / sampleSize > MAX_DIMENSION || opts.outHeight / sampleSize > MAX_DIMENSION) {
                    sampleSize *= 2;
                }

                opts.inJustDecodeBounds = false;
                opts.inSampleSize = sampleSize;

                is = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
                if (is != null) is.close();

                if (bitmap == null) {
                    postBitmapError(callback, "이미지를 불러오는 데 실패했습니다.");
                    return;
                }
                postBitmapSuccess(callback, bitmap);
            } catch (Exception e) {
                postBitmapError(callback, "이미지 처리 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }

    /**
     * 크롭된 비트맵을 내부 저장소에 저장하고 경로 반환 (5번 수정사항)
     */
    public void saveCroppedBitmap(Bitmap croppedBitmap, ImageCallback callback) {
        executor.execute(() -> {
            try {
                File imagesDir = new File(context.getFilesDir(), "diary_images");
                if (!imagesDir.exists()) imagesDir.mkdirs();

                String filename = "bg_" + System.currentTimeMillis() + ".jpg";
                File outFile = new File(imagesDir, filename);

                FileOutputStream fos = new FileOutputStream(outFile);
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, fos);
                fos.flush();
                fos.close();

                postSuccess(callback, outFile.getAbsolutePath());
            } catch (Exception e) {
                postError(callback, "배경 이미지 저장에 실패했습니다.");
            }
        });
    }

    private void postBitmapSuccess(BitmapCallback callback, Bitmap bitmap) {
        mainHandler.post(() -> callback.onSuccess(bitmap));
    }

    private void postBitmapError(BitmapCallback callback, String msg) {
        mainHandler.post(() -> callback.onError(msg));
    }
}
