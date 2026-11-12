package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import java.time.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(8)
public record SaatSecimi(JdbcTemplate db,Takvim takvim,java.util.Optional<Rezervasyon> rezervasyon) implements Bolme {
    public String goster(HttpSession s) {
        if(!rol(s,"Hasta")) return "";
        var liste=db.queryForList("""
                select concat(p.id, '-', h.id) id, concat(k.ad,' / ',b.ad,' / ',p.ad,' (',p.uzmanlik,') / ',h.ad,' · ',h.sure,' dk · ',h.ucret,' TL') ad
                from sunum x join kisi p on p.id=x.kisi join hizmet h on h.id=x.hizmet
                join bolum b on b.id=h.bolum join kurum k on k.id=b.kurum order by k.ad,b.ad,p.ad
                """);
        return "<section><h2>Randevu ara</h2><form action='/saatler'>"+sec("secim","Çalışan ve hizmet",liste)+alan("gun","Gün","date")+"<button>Uygun saatleri bul</button></form></section>";
    }

    @GetMapping(value="/saatler",produces="text/html;charset=UTF-8")
    @ResponseBody
    String saatler(HttpSession s,@RequestParam String secim,@RequestParam LocalDate gun) {
        long hasta=yetki(s,"Hasta");
        if(!secim.matches("[0-9]+-[0-9]+")) throw new IllegalArgumentException("Hizmet seçimi geçersiz.");
        String[] ikili=secim.split("-");
        var teklif=takvim.hesapla(hasta,new Takvim.Secim(Long.parseLong(ikili[0]),Long.parseLong(ikili[1]),gun));
        StringBuilder html=new StringBuilder("<section><h2>"+gun+" · Uygun saatler</h2>");
        if(teklif.saatler().isEmpty()) html.append("<p>Bu gün için boş saat yok.</p>");
        else if(rezervasyon.isEmpty()) html.append(teklif.saatler());
        else {
            html.append("<form method='post' action='/ayir'><input type='hidden' name='anahtar' value='"+s.getAttribute("anahtar")+"'><input type='hidden' name='kisi' value='"+ikili[0]+"'><input type='hidden' name='hizmet' value='"+ikili[1]+"'><input type='hidden' name='gun' value='"+gun+"'><label>Başlangıç<select name='ilk'>");
            teklif.saatler().forEach(t -> html.append("<option>").append(t).append("</option>"));
            html.append("</select></label><button>Randevu talebi oluştur</button></form>");
        }
        return cerceve(html+"</section><a href='/'>Panele dön</a>");
    }
}
