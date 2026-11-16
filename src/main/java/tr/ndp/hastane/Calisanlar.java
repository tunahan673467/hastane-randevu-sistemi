package tr.ndp.hastane;

import static tr.ndp.hastane.Oturum.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static tr.ndp.hastane.Sayfa.*;

@Controller
public record Calisanlar(Katalog k, HesapServisi hesaplar) implements Bolme {
    public String goster(HttpSession s) {
        if(!rol(s,"Yönetici")) return "";
        return "<div class='notice'><h3>Çalışan hesapları</h3><p>Önce şube ve bölüm oluşturun. Aşağıdan doktor veya sağlık personeli hesabı açın. Çalışan, belirlediğiniz kullanıcı adı ve parolayla giriş ekranındaki <strong>Çalışan</strong> sekmesinden oturum açar. Kayıtlı hesapları bu sayfanın altındaki ekip listesinde görebilirsiniz.</p></div>" + form(s,"/calisan","Çalışan hesabı oluştur",GirisSayfasi.alanlar()+k.bolum()+alan("uzmanlik","Uzmanlık","text")+"<label>Rol<select name='tur'><option>Doktor</option><option>Sağlık personeli</option></select></label>","Kaydet")
                +form(s,"/ata","Çalışana hizmet ata",k.personel()+k.hizmet(),"Ata");
    }

    @PostMapping("/calisan")
    String calisan(HttpSession s,@RequestParam String ad,@RequestParam String hesap,@RequestParam String parola,@RequestParam long bolum,@RequestParam String uzmanlik,@RequestParam String tur) {
        yetki(s,"Yönetici");
        if(!List.of("Doktor","Sağlık personeli").contains(tur)) throw new IllegalArgumentException("Rol geçersiz.");
        hesaplar.ekle(ad,hesap,parola,tur,bolum,uzmanlik);
        s.setAttribute("bildirim", "İşlem başarıyla tamamlandı.");
        return "redirect:/?sayfa=calisanlar";
    }

    @PostMapping("/ata")
    String ata(HttpSession s,@RequestParam long kisi,@RequestParam long hizmet) {
        yetki(s,"Yönetici");
        int n=k.db().update("insert into sunum select p.id,h.id from kisi p join hizmet h on h.bolum=p.bolum where p.id=? and h.id=?",kisi,hizmet);
        if(n!=1) throw new IllegalArgumentException("Çalışan ve hizmet aynı bölümde olmalı.");
        s.setAttribute("bildirim", "İşlem başarıyla tamamlandı.");
        return "redirect:/?sayfa=calisanlar";
    }
}
