package vn.iotstar.ktqt03.util;

import java.util.Locale;
import vn.iotstar.ktqt03.dto.VideoCard;
import vn.iotstar.ktqt03.exception.ValidationException;

public final class Validation {
    public static final String DEFAULT_POSTER = "/assets/images/poster.svg";
    private Validation() {}
    public static String text(String value) { return value == null ? "" : value.trim(); }
    public static String username(String value) {
        String result = text(value);
        if (!result.matches("[A-Za-z0-9_.-]{3,50}"))
            throw new ValidationException("Tên đăng nhập gồm 3–50 chữ, số hoặc dấu _ . -.");
        return result;
    }
    public static String email(String value) {
        String result = text(value).toLowerCase(Locale.ROOT);
        if (result.length() > 150 || !result.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new ValidationException("Email không hợp lệ hoặc dài quá 150 ký tự.");
        return result;
    }
    public static String required(String value, int max, String label) {
        String result = text(value);
        if (result.isEmpty() || result.length() > max)
            throw new ValidationException(label + " phải có từ 1 đến " + max + " ký tự.");
        return result;
    }
    public static void password(String value) {
        if (value == null || value.length() < 8 || value.length() > 128)
            throw new ValidationException("Mật khẩu phải có 8–128 ký tự.");
    }
    public static String id(String value) { return required(value, 50, "Mã video"); }
    public static String poster(String value) {
        String result = text(value);
        if (result.isEmpty()) return DEFAULT_POSTER;
        if (result.length() > 50 || !result.matches("/assets/images/[A-Za-z0-9_-]+\\.(?i:svg|png|jpe?g|webp|gif)"))
            throw new ValidationException("Poster phải là ảnh trong /assets/images/, tối đa 50 ký tự.");
        return result;
    }
    public static int nonNegativeInt(String value, String label) {
        try {
            int number = text(value).isEmpty() ? 0 : Integer.parseInt(text(value));
            if (number < 0) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException ex) {
            throw new ValidationException(label + " phải là số nguyên từ 0 đến 2147483647.");
        }
    }
    public static void video(VideoCard form) {
        form.setTitle(required(form.getTitle(), 200, "Tiêu đề"));
        form.setPoster(poster(form.getPoster()));
        if (form.getDescription() != null && form.getDescription().length() > 500)
            throw new ValidationException("Mô tả không được dài quá 500 ký tự.");
        if (form.getViews() < 0) throw new ValidationException("Lượt xem không được âm.");
        if (form.getCategoryId() == null || form.getCategoryId() <= 0)
            throw new ValidationException("Vui lòng chọn category hợp lệ.");
    }
}
