package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;
import java.math.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(5)
public record Hizmetler(Katalog k) implements Bolme {
    public String goster(HttpSession s) {
        if(!rol(s,"Yönetici")) return "";
        return form(s,"/hizmet","Hizmet tanımla",k.bolum()+alan("ad","Hizmet adı","text")+alan("sure","Süre (dk)","number")+alan("ucret","Ücret (TL)","number"),"Hizmet ekle");
    }

    @PostMapping("/hizmet")
    String hizmet(HttpSession s,@RequestParam long bolum,@RequestParam String ad,@RequestParam int sure,@RequestParam BigDecimal ucret) {
        yetki(s,"Yönetici");
        k.db().update("insert into hizmet(bolum,ad,sure,ucret) values(?,?,?,?)",bolum,ad.strip(),sure,ucret.setScale(2,RoundingMode.UNNECESSARY));
        return "redirect:/";
    }
}
