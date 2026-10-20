package com.ldh.quiz;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public final class QuizData {
    /** t = tieu de, m = meo nho, s = cac doan nguyen van ([[...]] la o trong, "## " dau dong la tieu de). */
    public static final class Q {
        public String t;
        public String m;
        public List<String> s;
    }

    private static List<Q> cache;

    private QuizData() {}

    public static synchronized List<Q> all() {
        if (cache == null) {
            try (InputStream in = QuizData.class.getResourceAsStream("/assets/ldhquiz/quiz.json");
                 InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                cache = new Gson().fromJson(r, new TypeToken<List<Q>>() {}.getType());
            } catch (Exception e) {
                e.printStackTrace();
                cache = List.of();
            }
        }
        return cache;
    }
}
