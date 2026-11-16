package tr.ndp.hastane;

import jakarta.servlet.http.*;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(1)
public record Kimlik(HesapServisi hesaplar) implements Bolme {
    static String alanlar() {
        return alan("ad", "Ad soyad", "text") + alan("hesap", "Kullanıcı adı", "text")
                + alan("parola", "Parola (en az 8 karakter)", "password");
    }

    public String goster(HttpSession s) {
        return "";
    }

    public String girisSayfasi(HttpSession s, String secim) {
        boolean kurulum = hesaplar.kurulum();
        String tip = Set.of("yonetici", "hasta", "calisan", "kayit", "kurulum").contains(secim) ? secim : "yonetici";
        String baslik = switch (tip) {
            case "hasta" -> "Hasta girişi";
            case "calisan" -> "Çalışan girişi";
            case "kayit" -> "Hasta hesabı oluştur";
            case "kurulum" -> "Yönetici hesabı oluştur";
            default -> "Yönetici girişi";
        };
        String tabs = "<div class='auth-tabs'><a class='" + (tip.equals("yonetici") ? "selected" : "")
                + "' href='/?giris=yonetici'>Yönetici</a><a class='" + (tip.equals("hasta") ? "selected" : "")
                + "' href='/?giris=hasta'>Hasta</a><a class='" + (tip.equals("calisan") ? "selected" : "")
                + "' href='/?giris=calisan'>Çalışan</a></div>";
        String icerik;
        if (tip.equals("kurulum")) {
            String onay = kurulum ? "" : "<p class='hint'>Yeni yöneticiyi eklemek için mevcut bir yönetici hesabıyla doğrulayın.</p>"
                    + alan("yetkiliHesap", "Mevcut yöneticinin kullanıcı adı", "text")
                    + alan("yetkiliParola", "Mevcut yöneticinin parolası", "password");
            icerik = form(s, "/kurulum", baslik, alanlar() + onay, "Yönetici oluştur");
        } else if (tip.equals("kayit")) {
            icerik = form(s, "/hasta", baslik, alanlar(), "Hesap oluştur");        } else {
            String beklenen = tip.equals("yonetici") ? "Yönetici" : tip.equals("hasta") ? "Hasta" : "Çalışan";
            String alanlar = "<input type='hidden' name='beklenen' value='" + beklenen + "'>"
                    + "<label>Kullanıcı adı<input required name='hesap' autocomplete='username' placeholder='Kullanıcı adınızı girin'></label>"
                    + "<label>Parola<input required type='password' name='parola' autocomplete='current-password' placeholder='Parolanızı girin'></label>";
            icerik = form(s, "/giris", baslik, alanlar, "Giriş yap");
        }
        if (tip.equals("calisan")) icerik += "<div class='notice'><h3>Çalışan hesabı nasıl açılır?</h3><p>Yöneticiniz, panelindeki <strong>Çalışanlar</strong> sayfasından doktor veya sağlık personeli hesabınızı oluşturur. Size verilen kullanıcı adı ve parolayla buradan giriş yapabilirsiniz. Girişten sonra kendi randevularınızı ve bekleyen taleplerinizi görebilirsiniz.</p></div>";
        String bildirim = (String) s.getAttribute("bildirim");
        s.removeAttribute("bildirim");
        String mesaj = bildirim == null ? "" : "<div role='status' class='success'>" + metin(bildirim) + "</div>";
        String yoneticiLink = "<a href='/?giris=kurulum'>Yönetici hesabı oluştur ↗</a>";
        return belge("<div class='auth-layout'><div class='auth-story'>" + marka()
                + "<div class='story-main'><span class='pill'>HASTANE RANDEVU SİSTEMİ</span><h1>Sağlık hizmetleri,<br>tek bir yerde.</h1>"
                + "<p>Randevularınızı takip edin.<br>Ekibinizi ve çalışma takviminizi yönetin.</p>"
                + "<div class='story-features'><div>01 <span>Kolay randevu planlama</span></div><div>02 <span>Düzenli ekip yönetimi</span></div><div>03 <span>Kişisel hesap ve oturum</span></div></div></div>"
                + "<div class='story-footer'>KLİNİK <span>Hastane & klinik randevu sistemi</span></div></div>"
                + "<main class='auth-main'><div class='auth-top'>Hastane & Klinik <span class='dot'></span> Randevu Sistemi</div><div class='auth-box'>"
                + "<p class='eyebrow'>HOŞ GELDİNİZ</p><h2>Gününüzü kolaylaştırın.</h2><p class='muted'>Hesap türünüzü seçerek devam edin.</p>"
                + tabs + mesaj + icerik + "<div class='auth-links'><a href='/?giris=kayit'>Hasta hesabı oluştur</a>" + yoneticiLink + "</div>"
                + (kurulum ? "<p class='hint'>İlk kullanım için bir yönetici hesabı oluşturun.</p>" : "")
                + "</div><footer>Kendi hesabınızla giriş yaparak işlemlerinize devam edin.</footer></main></div>");
    }

    @PostMapping("/kurulum")
    String kurulum(HttpSession s, @RequestParam String ad, @RequestParam String hesap, @RequestParam String parola, @RequestParam(defaultValue = "") String yetkiliHesap, @RequestParam(defaultValue = "") String yetkiliParola) {
        if (hesaplar.kurulum()) hesaplar.ekle(ad, hesap, parola, "Yönetici", null, null);
        else {
            var yetkili = hesaplar.gir(yetkiliHesap, yetkiliParola);
            hesaplar.yoneticiEkle(yetkili.id(), ad, hesap, parola);
        }
        s.setAttribute("bildirim", "Yönetici hesabınız oluşturuldu. Şimdi giriş yapabilirsiniz.");
        return "redirect:/?giris=yonetici";
    }

    @PostMapping("/hasta")
    String hasta(HttpSession s, @RequestParam String ad, @RequestParam String hesap, @RequestParam String parola) {
        hesaplar.ekle(ad, hesap, parola, "Hasta", null, null);
        s.setAttribute("bildirim", "Hasta hesabınız oluşturuldu. Şimdi giriş yapabilirsiniz.");
        return "redirect:/?giris=hasta";
    }

    @PostMapping("/giris")
    String gir(HttpServletRequest req, @RequestParam String hesap, @RequestParam String parola,
               @RequestParam(defaultValue = "") String beklenen) {
        var k = hesaplar.gir(hesap, parola);
        boolean uygun = beklenen.isBlank() || beklenen.equals(k.rol())
                || (beklenen.equals("Çalışan") && Set.of("Doktor", "Sağlık personeli").contains(k.rol()));
        if (!uygun) throw new IllegalArgumentException("Bu hesap " + k.rol() + " rolünde. Giriş ekranında doğru hesap türünü seçin.");
        req.changeSessionId();
        var s = req.getSession();
        s.setAttribute("id", k.id());
        s.setAttribute("ad", k.ad());
        s.setAttribute("rol", k.rol());
        s.removeAttribute("bildirim");
        return "redirect:/";
    }

    @PostMapping("/cikis")
    String cik(HttpSession s) {
        String giris = rol(s, "Yönetici") ? "yonetici" : rol(s, "Hasta") ? "hasta" : "calisan";
        s.invalidate();
        return "redirect:/?giris=" + giris;
    }
}