package tr.ndp.hastane;

import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import static tr.ndp.hastane.Sayfa.*;

@Controller
@Order(1)
public record Kimlik(HesapServisi hesaplar) implements Bolme {
    static String alanlar() {
        return alan("ad","Ad soyad","text")+alan("hesap","Kullanıcı adı","text")+alan("parola","Parola (en az 8 karakter)","password");
    }

    public String goster(HttpSession s) {
        if(s.getAttribute("id")!=null) return form(s,"/cikis","Oturum","","Çıkış yap");
        if(hesaplar.kurulum()) return form(s,"/kurulum","İlk yönetici hesabı",alanlar(),"Hesap oluştur");
        return form(s,"/giris","Oturum aç",alan("hesap","Kullanıcı adı","text")+alan("parola","Parola","password"),"Giriş yap")
                +form(s,"/hasta","Hasta kaydı",alanlar(),"Hasta hesabı oluştur");
    }

    @PostMapping("/kurulum")
    String kurulum(@RequestParam String ad,@RequestParam String hesap,@RequestParam String parola) {
        hesaplar.ekle(ad,hesap,parola,"Yönetici",null,null);
        return "redirect:/";
    }

    @PostMapping("/hasta")
    String hasta(@RequestParam String ad,@RequestParam String hesap,@RequestParam String parola) {
        hesaplar.ekle(ad,hesap,parola,"Hasta",null,null);
        return "redirect:/";
    }

    @PostMapping("/giris")
    String gir(HttpServletRequest req,@RequestParam String hesap,@RequestParam String parola) {
        var k=hesaplar.gir(hesap,parola);
        req.changeSessionId();
        var s=req.getSession();
        s.setAttribute("id",k.id());
        s.setAttribute("ad",k.ad());
        s.setAttribute("rol",k.rol());
        return "redirect:/";
    }

    @PostMapping("/cikis")
    String cik(HttpSession s) { s.invalidate(); return "redirect:/"; }
}
