package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;

public final class Oturum {
    public static boolean rol(HttpSession s, String rol) {
        return rol.equals(s.getAttribute("rol"));
    }

    public static long yetki(HttpSession s, String... roller) {
        for (String rol : roller) {
            if (rol(s, rol)) return (Long) s.getAttribute("id");
        }
        throw new IllegalArgumentException("Bu işlem için uygun hesapla giriş yapın.");
    }

}
