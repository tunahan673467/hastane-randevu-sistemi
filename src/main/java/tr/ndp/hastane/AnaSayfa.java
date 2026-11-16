package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import static tr.ndp.hastane.Sayfa.*;

@RestController
public record AnaSayfa(List<Bolme> bolmeler, Kimlik kimlik, JdbcTemplate db) {
    private static final Map<String, Class<? extends Bolme>> EKRANLAR = Map.of(
            "subeler", Subeler.class, "hizmetler", Hizmetler.class, "calisanlar", Calisanlar.class,
            "mesailer", Mesailer.class, "randevu", SaatSecimi.class, "randevular", Randevular.class, "kararlar", Kararlar.class);
    private static final Map<String, String> BASLIKLAR = Map.of(
            "ozet", "Genel bakış", "subeler", "Şubeler & bölümler", "hizmetler", "Sağlık hizmetleri",
            "calisanlar", "Çalışanlar", "mesailer", "Çalışma takvimi", "randevu", "Randevu al",
            "randevular", "Randevular", "kararlar", "Bekleyen talepler", "yoneticiler", "Yönetici hesapları");

    @GetMapping(value = "/", produces = "text/html;charset=UTF-8")
    public String ana(HttpSession s, @RequestParam(defaultValue = "ozet") String sayfa,
                      @RequestParam(defaultValue = "yonetici") String giris) {
        if (s.getAttribute("anahtar") == null) s.setAttribute("anahtar", UUID.randomUUID().toString());
        if (s.getAttribute("id") == null) return kimlik.girisSayfasi(s, giris);
        if (!BASLIKLAR.containsKey(sayfa)) sayfa = "ozet";
        String icerik;
        if (sayfa.equals("ozet")) {
            icerik = ozet(s);
        } else if (sayfa.equals("yoneticiler")) {
            yetki(s, "Yönetici");
            icerik = "<p class='notice'>Her yönetici kendi hesabıyla giriş yapar. Yönetici hesapları tüm şubelere erişebilir.</p>"
                    + form(s, "/yonetici", "Yeni yönetici oluştur", Kimlik.alanlar(), "Yönetici ekle")
                    + kayitlar("Kayıtlı yöneticiler", "select ad yönetici,hesap kullanıcı from kisi where rol='Yönetici' order by ad");
        } else {
            icerik = bolme(s, EKRANLAR.get(sayfa));
            if (icerik.isEmpty()) throw new IllegalArgumentException("Bu sayfaya erişim yetkiniz yok.");
            if (rol(s, "Yönetici")) icerik += envanter(sayfa);
        }
        String bildirim = (String) s.getAttribute("bildirim");
        s.removeAttribute("bildirim");
        if (bildirim != null) icerik = "<div class='success' role='status'>✓ " + metin(bildirim) + "</div>" + icerik;
        String aciklama = sayfa.equals("ozet") ? "Hoş geldiniz, " + s.getAttribute("ad") + ". Günün planına birlikte bakalım."
                : "Kayıtlarınızı bu alandan görüntüleyebilir ve işlemlerinizi tamamlayabilirsiniz.";
        return panel(s, sayfa, BASLIKLAR.get(sayfa), aciklama, icerik);
    }

    private String bolme(HttpSession s, Class<? extends Bolme> tur) {
        return bolmeler.stream().filter(tur::isInstance).map(b -> b.goster(s)).findFirst().orElse("");
    }

    private String kayitlar(String baslik, String sql) {
        return "<section class='card'><div class='card-heading'><h2>" + baslik + "</h2><span class='muted'>Kayıtlar</span></div>"
                + tablo(db.queryForList(sql)) + "</section>";
    }

    private String envanter(String sayfa) {
        return switch (sayfa) {
            case "subeler" -> kayitlar("Kayıtlı şubeler", "select ad şube,ac açılış,kapa kapanış from kurum order by ad")
                    + kayitlar("Kayıtlı bölümler", "select k.ad şube,b.ad bölüm from bolum b join kurum k on k.id=b.kurum order by k.ad,b.ad");
            case "hizmetler" -> kayitlar("Hizmet kataloğu", "select k.ad şube,b.ad bölüm,h.ad hizmet,h.sure dakika,h.ucret ücret from hizmet h join bolum b on b.id=h.bolum join kurum k on k.id=b.kurum order by k.ad,b.ad,h.ad");
            case "calisanlar" -> kayitlar("Ekip listesi", "select p.ad çalışan,p.hesap kullanıcı,p.rol görev,p.uzmanlik uzmanlık,k.ad şube,b.ad bölüm from kisi p join bolum b on b.id=p.bolum join kurum k on k.id=b.kurum order by p.ad")
                    + kayitlar("Hizmet atamaları", "select p.ad çalışan,h.ad hizmet from sunum s join kisi p on p.id=s.kisi join hizmet h on h.id=s.hizmet order by p.ad,h.ad");
            case "mesailer" -> kayitlar("Tanımlı çalışma aralıkları", "select p.ad çalışan,m.gun gün,m.ilk başlangıç,m.son bitiş from mesai m join kisi p on p.id=m.kisi order by m.gun desc,p.ad");
            default -> "";
        };
    }

    private String istatistik(String etiket, long sayi, String alt) {
        return "<div class='stat'><span>" + etiket + "</span><strong>" + sayi + "</strong><small>" + alt + "</small></div>";
    }

    private String ozet(HttpSession s) {
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
                + hedef + "'>" + dugme + " ↗</a></div>" + bolme(s, Randevular.class);
    }

    @PostMapping("/yonetici")
    public RedirectView yonetici(HttpSession s, @RequestParam String ad, @RequestParam String hesap, @RequestParam String parola) {
        kimlik.hesaplar().yoneticiEkle(yetki(s, "Yönetici"), ad, hesap, parola);
        s.setAttribute("bildirim", "Yeni yönetici hesabı oluşturuldu. Kendi kullanıcı adıyla giriş yapabilir.");
        return new RedirectView("/?sayfa=yoneticiler");
    }
}