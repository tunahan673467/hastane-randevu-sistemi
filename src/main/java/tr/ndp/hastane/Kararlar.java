package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(10)
public record Kararlar(JdbcTemplate db) implements Bolme {
    public String goster(HttpSession s) {
        if(!rol(s,"Doktor") && !rol(s,"Sağlık personeli")) return "";
        var liste=db.queryForList("select r.id,concat(a.ad,' · ',h.ad,' · ',r.gun,' ',r.ilk) ad from randevu r join kisi a on a.id=r.hasta join hizmet h on h.id=r.hizmet where r.personel=? and r.durum='Bekliyor'",s.getAttribute("id"));
        if(liste.isEmpty()) return "<section><h2>Bekleyen talepler</h2><p>Değerlendirilecek talep yok.</p></section>";
        return form(s,"/karar","Talep değerlendir",sec("id","Randevu",liste)+"<label>Karar<select name='karar'><option>Onaylandı</option><option>Reddedildi</option></select></label>","Kararı kaydet");
    }

    @PostMapping("/karar")
    String karar(HttpSession s,@RequestParam long id,@RequestParam String karar) {
        long kisi=yetki(s,"Doktor","Sağlık personeli");
        if(!karar.equals("Onaylandı") && !karar.equals("Reddedildi")) throw new IllegalArgumentException("Geçersiz karar.");
        if(db.update("update randevu set durum=? where id=? and personel=? and durum='Bekliyor'",karar,id,kisi)!=1)
            throw new IllegalArgumentException("Bu talep artık değerlendirilemiyor.");
        return "redirect:/";
    }
}
