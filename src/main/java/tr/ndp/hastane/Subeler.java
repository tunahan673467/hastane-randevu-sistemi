package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(4)
public record Subeler(Katalog k) implements Bolme {
    public String goster(HttpSession s) {
        if(!rol(s,"Yönetici")) return "";
        return form(s,"/kurum","Şube tanımla",alan("ad","Şube adı","text")+alan("ac","Açılış","time")+alan("kapa","Kapanış","time"),"Şube ekle")
                +form(s,"/bolum","Bölüm tanımla",k.kurum()+alan("ad","Bölüm adı","text"),"Bölüm ekle");
    }

    @PostMapping("/kurum")
    String kurum(HttpSession s,@RequestParam String ad,@RequestParam LocalTime ac,@RequestParam LocalTime kapa) {
        yetki(s,"Yönetici");
        k.db().update("insert into kurum(ad,ac,kapa) values(?,?,?)",ad.strip(),ac,kapa);
        return "redirect:/";
    }

    @PostMapping("/bolum")
    String bolum(HttpSession s,@RequestParam long kurum,@RequestParam String ad) {
        yetki(s,"Yönetici");
        k.db().update("insert into bolum(kurum,ad) values(?,?)",kurum,ad.strip());
        return "redirect:/";
    }
}
