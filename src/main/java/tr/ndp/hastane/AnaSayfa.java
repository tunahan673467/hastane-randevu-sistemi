package tr.ndp.hastane;

import static tr.ndp.hastane.Oturum.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import static tr.ndp.hastane.Sayfa.*;

@RestController
public record AnaSayfa(List<Bolme> bolmeler, GirisSayfasi girisSayfasi, GenelBakis genelBakis,
                       KayitListeleri listeler, Yoneticiler yoneticiler) {
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
        if (s.getAttribute("id") == null) return girisSayfasi.girisSayfasi(s, giris);
        if (!BASLIKLAR.containsKey(sayfa)) sayfa = "ozet";
        String icerik;
        if (sayfa.equals("ozet")) {
            icerik = genelBakis.goster(s);
        } else if (sayfa.equals("yoneticiler")) {
            icerik = yoneticiler.goster(s);
        } else {
            icerik = bolme(s, EKRANLAR.get(sayfa));
            if (icerik.isEmpty()) throw new IllegalArgumentException("Bu sayfaya erişim yetkiniz yok.");
            if (rol(s, "Yönetici")) icerik += listeler.goster(sayfa);
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

}
