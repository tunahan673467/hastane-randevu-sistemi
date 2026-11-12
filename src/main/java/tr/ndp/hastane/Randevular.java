package tr.ndp.hastane;

import java.time.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(9)
public record Randevular(JdbcTemplate db,Rezervasyon servis) implements Bolme {
    public String goster(HttpSession s) {
        if(s.getAttribute("id")==null) return "";
        String sql="""
                select r.id,k.ad şube,b.ad bölüm,h.ad hizmet,a.ad hasta,p.ad çalışan,r.gun gün,
                r.ilk başlangıç,r.son bitiş,datediff('minute',r.ilk,r.son) dakika,r.ucret ücret,r.durum
                from randevu r join kisi a on a.id=r.hasta join kisi p on p.id=r.personel
                join hizmet h on h.id=r.hizmet join bolum b on b.id=h.bolum join kurum k on k.id=b.kurum
                """;
        String sira=" order by r.gun,r.ilk";
        var liste=rol(s,"Yönetici") ? db.queryForList(sql+sira) :
                db.queryForList(sql+(rol(s,"Hasta") ? " where r.hasta=?" : " where r.personel=?")+sira,s.getAttribute("id"));
        return "<section><h2>Randevular</h2>"+tablo(liste)+"</section>";
    }

    @PostMapping("/ayir")
    String ayir(HttpSession s,@RequestParam long kisi,@RequestParam long hizmet,@RequestParam LocalDate gun,@RequestParam LocalTime ilk) {
        servis.ayir(yetki(s,"Hasta"),new Takvim.Secim(kisi,hizmet,gun),ilk);
        return "redirect:/";
    }
}
