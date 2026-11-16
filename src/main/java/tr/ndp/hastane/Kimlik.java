package tr.ndp.hastane;

import static tr.ndp.hastane.Oturum.*;

import jakarta.servlet.http.*;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
public record Kimlik(HesapServisi hesaplar) {
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