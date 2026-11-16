package tr.ndp.hastane;

import static tr.ndp.hastane.Oturum.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import static tr.ndp.hastane.Sayfa.*;

@Component
public record GenelBakis(JdbcTemplate db, Randevular randevular) {
    private String istatistik(String etiket, long sayi, String alt) {
        return "<div class='stat'><span>" + etiket + "</span><strong>" + sayi + "</strong><small>" + alt + "</small></div>";
    }

    public String goster(HttpSession s) {
        String filtre = rol(s, "Yönetici") ? "" : rol(s, "Hasta") ? " and hasta=?" : " and personel=?";
        Object[] parametre = rol(s, "Yönetici") ? new Object[0] : new Object[]{s.getAttribute("id")};
        long toplam = db.queryForObject("select count(*) from randevu where 1=1" + filtre, Long.class, parametre);
        long bekleyen = db.queryForObject("select count(*) from randevu where durum='Bekliyor'" + filtre, Long.class, parametre);
        long onayli = db.queryForObject("select count(*) from randevu where durum='Onaylandı'" + filtre, Long.class, parametre);
        String kartlar = istatistik("Toplam randevu", toplam, "Tüm kayıtlar")
                + istatistik("Onay bekleyen", bekleyen, "Değerlendirme aşamasında")
                + istatistik("Onaylanan", onayli, "Onayı tamamlanan randevular");
        String hedef = rol(s, "Yönetici") ? "subeler" : rol(s, "Hasta") ? "randevu" : "kararlar";
        String dugme = rol(s, "Yönetici") ? "Şubeleri yönet" : rol(s, "Hasta") ? "Randevu al" : "Talepleri değerlendir";
        String yonlendirme = rol(s, "Yönetici")
                ? "Şube ve bölüm → Hizmet → Çalışan ve hizmet ataması → Çalışma takvimi"
                : rol(s, "Hasta") ? "Hizmet seç → Uygun saat bul → Randevu talebi oluştur"
                : "Bekleyen talebi seç → Detayları incele → Onayla veya reddet";
        return "<div class='stats'>" + kartlar + "</div><div class='welcome-banner'><div><span class='eyebrow'>HER ŞEY BİR PLANLA BAŞLAR</span>"
                + "<h2>Bir sonraki adımınız hazır.</h2><p>" + yonlendirme + "</p></div><a class='button light' href='/?sayfa="
                + hedef + "'>" + dugme + " ↗</a></div>" + randevular.goster(s);
    }

}
