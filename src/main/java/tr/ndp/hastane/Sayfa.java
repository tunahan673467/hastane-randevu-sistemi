package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.web.util.HtmlUtils;

public final class Sayfa {
    public static String metin(Object s) {
        return HtmlUtils.htmlEscape(String.valueOf(s));
    }

    public static boolean rol(HttpSession s, String rol) {
        return rol.equals(s.getAttribute("rol"));
    }

    public static long yetki(HttpSession s, String... roller) {
        for (String rol : roller) if (rol(s,rol)) return (Long)s.getAttribute("id");
        throw new IllegalArgumentException("Bu işlem için uygun hesapla giriş yapın.");
    }

    public static String alan(String ad,String etiket,String tur) {
        String ek = tur.equals("number") ? " min='0' step='any'" : "";
        return "<label>"+etiket+"<input required name='"+ad+"' type='"+tur+"'"+ek+"></label>";
    }

    public static String sec(String ad,String etiket,List<Map<String,Object>> satirlar) {
        StringBuilder html=new StringBuilder("<label>"+etiket+"<select required name='"+ad+"'><option value=''>Seçiniz</option>");
        for(var r:satirlar) html.append("<option value='").append(metin(r.get("ID"))).append("'>").append(metin(r.get("ad"))).append("</option>");
        return html+"</select></label>";
    }

    public static String form(HttpSession s,String yol,String baslik,String alanlar,String dugme) {
        return "<section><h2>"+baslik+"</h2><form method='post' action='"+yol+"'><input type='hidden' name='anahtar' value='"
                +s.getAttribute("anahtar")+"'>"+alanlar+"<button>"+dugme+"</button></form></section>";
    }

    public static String tablo(List<Map<String,Object>> sonuc) {
        if(sonuc.isEmpty()) return "<p>Henüz kayıt yok.</p>";
        StringBuilder t=new StringBuilder("<div class='kaydir'><table><thead><tr>");
        sonuc.get(0).keySet().forEach(k -> t.append("<th>").append(metin(k)).append("</th>"));
        t.append("</tr></thead><tbody>");
        for(var r:sonuc) {
            t.append("<tr>");
            r.values().forEach(v -> t.append("<td>").append(metin(v)).append("</td>"));
            t.append("</tr>");
        }
        return t+"</tbody></table></div>";
    }

    public static String cerceve(String icerik) {
        return "<!doctype html><html lang='tr'><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>Klinik | Randevu sistemi</title><link rel='stylesheet' href='/stil.css'><main><header><small>KLİNİK  /  YEREL HASTANE SİSTEMİ</small><h1>Randevu merkezi</h1></header>"+icerik+"</main></html>";
    }
}
