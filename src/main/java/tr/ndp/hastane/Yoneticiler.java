package tr.ndp.hastane;

import static tr.ndp.hastane.Oturum.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import static tr.ndp.hastane.Sayfa.*;

@Controller
public record Yoneticiler(HesapServisi hesaplar, KayitListeleri listeler) {
    public String goster(HttpSession s) {
        yetki(s, "Yönetici");
        return "<p class='notice'>Her yönetici kendi hesabıyla giriş yapar. Yönetici hesapları tüm şubelere erişebilir.</p>"
                + form(s, "/yonetici", "Yeni yönetici oluştur", GirisSayfasi.alanlar(), "Yönetici ekle")
                + listeler.kayitlar("Kayıtlı yöneticiler", "select ad yönetici,hesap kullanıcı from kisi where rol='Yönetici' order by ad");
    }

    @PostMapping("/yonetici")
    public RedirectView yonetici(HttpSession s, @RequestParam String ad, @RequestParam String hesap, @RequestParam String parola) {
        hesaplar.yoneticiEkle(yetki(s, "Yönetici"), ad, hesap, parola);
        s.setAttribute("bildirim", "Yeni yönetici hesabı oluşturuldu. Kendi kullanıcı adıyla giriş yapabilir.");
        return new RedirectView("/?sayfa=yoneticiler");
    }
}
