package tr.ndp.hastane;

import static tr.ndp.hastane.Oturum.*;

import jakarta.servlet.http.HttpSession;
import java.time.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
public record SaatSecimi(JdbcTemplate db, Takvim takvim) implements Bolme {
    public String goster(HttpSession s) {
        if (!rol(s, "Hasta")) return "";
        var liste = db.queryForList("""
                select concat(p.id, '-', h.id) id, concat(k.ad,' / ',b.ad,' / ',p.ad,' (',p.uzmanlik,') / ',h.ad,' · ',h.sure,' dk · ',h.ucret,' TL') ad
                from sunum x join kisi p on p.id=x.kisi join hizmet h on h.id=x.hizmet
                join bolum b on b.id=h.bolum join kurum k on k.id=b.kurum order by k.ad,b.ad,p.ad
                """);
        if (liste.isEmpty()) return "<section><h2>Randevu ara</h2><div class='empty'><span class='empty-icon'>＋</span><h3>Hizmetler hazırlanıyor</h3><p>Kurumunuz çalışan ve hizmet tanımlarını tamamladığında burada seçim yapabilirsiniz.</p></div></section>";
        return "<section class='card'><div class='card-heading'><h2>Hizmet ve gün seçimi</h2><span class='muted'>Adım 1 / 2</span></div>"
                + "<form class='fields' action='/saatler'>" + sec("secim", "Çalışan ve hizmet", liste) + alan("gun", "Gün", "date")
                + "<div class='form-action'><button>Uygun saatleri bul ↗</button></div></form></section>";
    }

    @GetMapping(value = "/saatler", produces = "text/html;charset=UTF-8")
    @ResponseBody
    String saatler(HttpSession s, @RequestParam String secim, @RequestParam LocalDate gun) {
        long hasta = yetki(s, "Hasta");
        if (!secim.matches("[0-9]+-[0-9]+")) throw new IllegalArgumentException("Hizmet seçimi geçersiz.");
        String[] ikili = secim.split("-");
        long kisi = Long.parseLong(ikili[0]);
        long hizmet = Long.parseLong(ikili[1]);
        var teklif = takvim.hesapla(hasta, new Takvim.Secim(kisi, hizmet, gun));
        String detay = db.queryForObject("""
                select concat(k.ad,' / ',b.ad,' / ',p.ad,' / ',h.ad)
                from sunum x join kisi p on p.id=x.kisi join hizmet h on h.id=x.hizmet
                join bolum b on b.id=h.bolum join kurum k on k.id=b.kurum where p.id=? and h.id=?
                """, String.class, kisi, hizmet);
        StringBuilder html = new StringBuilder("<div class='notice'><strong>" + metin(detay) + "</strong><br>"
                + gun.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " · " + teklif.sure() + " dakika · " + teklif.ucret() + " TL</div>");
        if (teklif.saatler().isEmpty()) {
            html.append("<section><div class='empty'><span class='empty-icon'>◷</span><h3>Bu gün için boş saat yok</h3><p>Başka bir gün veya çalışan seçerek tekrar deneyin.</p></div></section>");
        } else {
            StringBuilder secenekler = new StringBuilder("<label>Başlangıç saati<select required name='ilk'><option value=''>Saat seçiniz</option>");
            teklif.saatler().forEach(t -> secenekler.append("<option>").append(t).append("</option>"));
            secenekler.append("</select></label><input type='hidden' name='kisi' value='").append(kisi)
                    .append("'><input type='hidden' name='hizmet' value='").append(hizmet)
                    .append("'><input type='hidden' name='gun' value='").append(gun).append("'>");
            html.append(form(s, "/ayir", "Size uygun saati seçin", secenekler.toString(), "Randevu talebi oluştur"));
        }
        html.append("<a class='text-link' href='/?sayfa=randevu'>← Hizmet veya günü değiştir</a>");
        return panel(s, "randevu", "Uygun saatler", "Talebiniz oluşturulduktan sonra çalışanın onayına gönderilir.", html.toString());
    }
}