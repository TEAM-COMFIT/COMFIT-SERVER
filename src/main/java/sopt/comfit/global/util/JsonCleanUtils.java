package sopt.comfit.global.util;

public class JsonCleanUtils {

    public static String clean(String content) {
        if (content == null) return null;

        String cleaned = content.trim();
        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        return cleaned.trim();
    }

}
