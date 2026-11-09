package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(6)
public record Calisanlar(Katalog k, HesapServisi hesaplar) implements Bolme {
    public String goster(HttpSession s) {
        if(!rol(s,"Yönetici")) return "";
        return form(s,"/calisan","Çalışan kaydet",Kimlik.alanlar()+k.bolum()+alan("uzmanlik","Uzmanlık","text")+"<label>Rol<select name='tur'><option>Doktor</option><option>Sağlık personeli</option></select></label>","Kaydet")
                +form(s,"/ata","Çalışana hizmet ata",k.personel()+k.hizmet(),"Ata");
    }

    @PostMapping("/calisan")
    String calisan(HttpSession s,@RequestParam String ad,@RequestParam String hesap,@RequestParam String parola,@RequestParam long bolum,@RequestParam String uzmanlik,@RequestParam String tur) {
        yetki(s,"Yönetici");
        if(!List.of("Doktor","Sağlık personeli").contains(tur)) throw new IllegalArgumentException("Rol geçersiz.");
        hesaplar.ekle(ad,hesap,parola,tur,bolum,uzmanlik);
        return "redirect:/";
    }

    @PostMapping("/ata")
    String ata(HttpSession s,@RequestParam long kisi,@RequestParam long hizmet) {
        yetki(s,"Yönetici");
        int n=k.db().update("insert into sunum select p.id,h.id from kisi p join hizmet h on h.bolum=p.bolum where p.id=? and h.id=?",kisi,hizmet);
        if(n!=1) throw new IllegalArgumentException("Çalışan ve hizmet aynı bölümde olmalı.");
        return "redirect:/";
    }
}
