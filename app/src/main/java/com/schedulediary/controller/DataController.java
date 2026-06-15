package com.schedulediary.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.schedulediary.database.AppDatabase;
import com.schedulediary.model.AdDiary;
import com.schedulediary.model.AdPage;
import com.schedulediary.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 사용자가 입력한 모든 데이터를 저장, 관리, 로드하는 클래스 (Design.md §5)
 */
public class DataController {

    private static final String PREF_NAME = "ScheduleDiaryPrefs";
    private static final String KEY_LOGGED_IN_USER_ID = "loggedInUserId";
    private static final int MAX_PAGES_PER_DIARY = 100;

    private final AppDatabase db;
    private final SharedPreferences prefs;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private static volatile DataController instance;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private DataController(Context context) {
        db = AppDatabase.getInstance(context);
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static DataController getInstance(Context context) {
        if (instance == null) {
            synchronized (DataController.class) {
                if (instance == null) {
                    instance = new DataController(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    // ────────────────────────────────────────────────
    // 회원가입
    // ────────────────────────────────────────────────

    /**
     * DB에 신규 회원 정보 등록 (Design.md §5 registerNewUser)
     */
    public void registerNewUser(String email, String password, String name, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                // 이메일 중복 체크
                int count = db.userDao().countByEmail(email);
                if (count > 0) {
                    postError(callback, "이미 사용 중인 이메일입니다.");
                    return;
                }
                String hash = hashPassword(password);
                User user = new User(email, hash, name);
                long id = db.userDao().insertUser(user);
                if (id > 0) {
                    postSuccess(callback, true);
                } else {
                    postError(callback, "회원가입에 실패했습니다. 다시 시도해주세요.");
                }
            } catch (Exception e) {
                postError(callback, "오류가 발생했습니다: " + e.getMessage());
            }
        });
    }

    // ────────────────────────────────────────────────
    // 로그인 / 로그아웃
    // ────────────────────────────────────────────────

    public void login(String email, String password, Callback<User> callback) {
        executor.execute(() -> {
            try {
                String hash = hashPassword(password);
                User user = db.userDao().login(email, hash);
                if (user != null) {
                    // 세션 저장
                    prefs.edit().putInt(KEY_LOGGED_IN_USER_ID, user.getUserId()).apply();
                    postSuccess(callback, user);
                } else {
                    // 이메일 존재 여부로 에러 구분
                    int count = db.userDao().countByEmail(email);
                    if (count == 0) {
                        postError(callback, "등록되지 않은 계정입니다.");
                    } else {
                        postError(callback, "비밀번호가 올바르지 않습니다.");
                    }
                }
            } catch (Exception e) {
                postError(callback, "로그인 중 오류가 발생했습니다.");
            }
        });
    }

    public void logout() {
        prefs.edit().remove(KEY_LOGGED_IN_USER_ID).apply();
    }

    public int getLoggedInUserId() {
        return prefs.getInt(KEY_LOGGED_IN_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        return getLoggedInUserId() != -1;
    }

    // ────────────────────────────────────────────────
    // 다이어리 CRUD
    // ────────────────────────────────────────────────

    public void loadDiaryList(int userId, Callback<List<AdDiary>> callback) {
        executor.execute(() -> {
            List<AdDiary> list = db.diaryDao().getDiariesByOwnerSorted(userId);
            postSuccess(callback, list);
        });
    }

    /** 다이어리 드래그 정렬 결과 저장 (8번 수정사항) */
    public void updateDiaryOrder(List<AdDiary> orderedDiaries, Callback<Boolean> callback) {
        executor.execute(() -> {
            for (int i = 0; i < orderedDiaries.size(); i++) {
                db.diaryDao().updateSortOrder(orderedDiaries.get(i).getDiaryId(), i);
            }
            postSuccess(callback, true);
        });
    }

    public void createDiary(AdDiary diary, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                int maxOrder = db.diaryDao().getMaxSortOrder(diary.getOwnerId());
                diary.setSortOrder(maxOrder + 1);
                long id = db.diaryDao().insertDiary(diary);
                if (id > 0) {
                    postSuccess(callback, id);
                } else {
                    postError(callback, "다이어리 생성에 실패했습니다.");
                }
            } catch (Exception e) {
                postError(callback, "다이어리 생성 중 오류: " + e.getMessage());
            }
        });
    }

    public void updateDiary(AdDiary diary, Callback<Boolean> callback) {
        executor.execute(() -> {
            diary.setUpdatedAt(System.currentTimeMillis());
            db.diaryDao().updateDiary(diary);
            postSuccess(callback, true);
        });
    }

    public void deleteDiary(AdDiary diary, Callback<Boolean> callback) {
        executor.execute(() -> {
            db.diaryDao().deleteDiary(diary);
            postSuccess(callback, true);
        });
    }

    public void searchDiaries(int userId, String query, Callback<List<AdDiary>> callback) {
        executor.execute(() -> {
            List<AdDiary> list = db.diaryDao().searchDiaries(userId, query);
            postSuccess(callback, list);
        });
    }

    public void getDiaryById(int diaryId, Callback<AdDiary> callback) {
        executor.execute(() -> {
            AdDiary diary = db.diaryDao().getDiaryById(diaryId);
            postSuccess(callback, diary);
        });
    }

    // ────────────────────────────────────────────────
    // 페이지 CRUD (Design.md §5 savePageData, loadPageData)
    // ────────────────────────────────────────────────

    public void loadPageList(int diaryId, Callback<List<AdPage>> callback) {
        executor.execute(() -> {
            List<AdPage> list = db.pageDao().getPagesByDiary(diaryId);
            postSuccess(callback, list);
        });
    }

    public void loadPageData(int pageId, Callback<AdPage> callback) {
        executor.execute(() -> {
            AdPage page = db.pageDao().getPageById(pageId);
            postSuccess(callback, page);
        });
    }

    /**
     * 새 페이지 생성 - 위치 지정 가능 (Design.md §8 addPage)
     * position: 0=맨 앞, -1=맨 뒤, n=특정 위치
     */
    public void createPage(int diaryId, String title, int position, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                int total = db.pageDao().countPagesByDiary(diaryId);
                if (total >= MAX_PAGES_PER_DIARY) {
                    postError(callback, "페이지 한도(" + MAX_PAGES_PER_DIARY + "개)를 초과했습니다.");
                    return;
                }

                int insertAt;
                if (position == -1 || position > total) {
                    insertAt = total + 1;
                } else if (position == 0) {
                    insertAt = 1;
                } else {
                    insertAt = position;
                }

                // 삽입 위치 이후 페이지 번호 밀기
                db.pageDao().shiftPagesDown(diaryId, insertAt);

                AdPage page = new AdPage(diaryId, title, insertAt);
                long id = db.pageDao().insertPage(page);
                postSuccess(callback, id);
            } catch (Exception e) {
                postError(callback, "페이지 생성 중 오류: " + e.getMessage());
            }
        });
    }

    public void savePageData(AdPage page, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                page.setUpdatedAt(System.currentTimeMillis());
                db.pageDao().updatePage(page);
                postSuccess(callback, true);
            } catch (Exception e) {
                postError(callback, "저장에 실패했습니다. 다시 시도해주세요.");
            }
        });
    }

    public void deletePage(AdPage page, Callback<Boolean> callback) {
        executor.execute(() -> {
            db.pageDao().deletePage(page);
            postSuccess(callback, true);
        });
    }

    public void searchPages(int diaryId, String query, Callback<List<AdPage>> callback) {
        executor.execute(() -> {
            List<AdPage> list = db.pageDao().searchPages(diaryId, query);
            postSuccess(callback, list);
        });
    }

    /** 페이지 드래그 정렬 결과 저장 - pageNumber 재할당 (8번 수정사항) */
    public void updatePageOrder(List<AdPage> orderedPages, Callback<Boolean> callback) {
        executor.execute(() -> {
            for (int i = 0; i < orderedPages.size(); i++) {
                int newNumber = i + 1;
                AdPage page = orderedPages.get(i);
                page.setPageNumber(newNumber);
                db.pageDao().updatePageNumber(page.getPageId(), newNumber);
            }
            postSuccess(callback, true);
        });
    }

    // ────────────────────────────────────────────────
    // 사용자 정의 커버 색상 프리셋 (2번 수정사항)
    // SharedPreferences에 HEX 색상 코드 목록(쉼표 구분)으로 저장
    // ────────────────────────────────────────────────

    private static final String KEY_CUSTOM_COLOR_PRESETS = "customColorPresets";
    private static final int MAX_CUSTOM_PRESETS = 12;

    /** 저장된 사용자 정의 색상 프리셋 목록 반환 (없으면 빈 리스트) */
    public List<String> getCustomColorPresets() {
        String raw = prefs.getString(KEY_CUSTOM_COLOR_PRESETS, "");
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return result;
        for (String hex : raw.split(",")) {
            if (!hex.trim().isEmpty()) result.add(hex.trim());
        }
        return result;
    }

    /** 새 색상 프리셋 추가 (중복 시 무시, 최대 개수 초과 시 가장 오래된 것 제거) */
    public void addCustomColorPreset(String hex) {
        List<String> presets = getCustomColorPresets();
        if (presets.contains(hex)) return;
        presets.add(hex);
        while (presets.size() > MAX_CUSTOM_PRESETS) {
            presets.remove(0);
        }
        prefs.edit().putString(KEY_CUSTOM_COLOR_PRESETS, String.join(",", presets)).apply();
    }

    // ────────────────────────────────────────────────
    // 유틸리티
    // ────────────────────────────────────────────────

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private <T> void postSuccess(Callback<T> callback, T result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private <T> void postError(Callback<T> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
