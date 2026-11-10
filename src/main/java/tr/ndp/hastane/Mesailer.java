package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(7)
public record Mesailer(Katalog k) implements Bolme {
    public String goster(HttpSession s) {
        if(!rol(s,"Yönetici")) return "";
        return form(s,"/mesai","Çalışma aralığı",k.personel()+alan("gun","Gün","date")+alan("ilk","Başlangıç","time")+alan("son","Bitiş","time"),"Aralık ekle");
    }

    @PostMapping("/mesai")
    String mesai(HttpSession s,@RequestParam long kisi,@RequestParam LocalDate gun,@RequestParam LocalTime ilk,@RequestParam LocalTime son) {
        yetki(s,"Yönetici");
        if(gun.isBefore(LocalDate.now())) throw new IllegalArgumentException("Geçmiş gün seçilemez.");
        int n=k.db().update("insert into mesai select p.id,?,?,? from kisi p join bolum b on b.id=p.bolum join kurum k on k.id=b.kurum where p.id=? and k.ac<=? and k.kapa>=?",gun,ilk,son,kisi,ilk,son);
        if(n!=1) throw new IllegalArgumentException("Aralık şubenin çalışma saatleri içinde olmalı.");
        return "redirect:/";
    }
}
