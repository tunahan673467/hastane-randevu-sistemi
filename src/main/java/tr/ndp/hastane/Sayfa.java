package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.util.HtmlUtils;

public final class Sayfa {
    public static String metin(Object s) {
        return s == null ? "—" : HtmlUtils.htmlEscape(String.valueOf(s));
    }

    public static boolean rol(HttpSession s, String rol) {
        return rol.equals(s.getAttribute("rol"));
    }

    public static long yetki(HttpSession s, String... roller) {
        for (String rol : roller) {
            if (rol(s, rol)) return (Long) s.getAttribute("id");
        }
        throw new IllegalArgumentException("Bu işlem için uygun hesapla giriş yapın.");
    }

    public static String alan(String ad, String etiket, String tur) {
        String ek = switch (tur) {
            case "number" -> ad.equals("sure") ? " min='1' max='1440' step='1'" : " min='0' max='9999999999.99' step='0.01'";
            case "date" -> " min='" + java.time.LocalDate.now() + "'";
            case "password" -> " minlength='8' autocomplete='new-password'";
            default -> " maxlength='120'";
        };
        return "<label>" + metin(etiket) + "<input required name='" + ad + "' type='" + tur + "'" + ek + "></label>";
    }

    public static String sec(String ad, String etiket, List<Map<String, Object>> satirlar) {
        StringBuilder html = new StringBuilder("<label>" + metin(etiket) + "<select required name='" + ad + "'><option value=''>Seçiniz</option>");
        for (var r : satirlar) {
            html.append("<option value='").append(metin(r.get("ID"))).append("'>").append(metin(r.get("ad"))).append("</option>");
        }
        return html + "</select></label>";
    }

    public static String token(HttpSession s) {
        return "<input type='hidden' name='anahtar' value='" + metin(s.getAttribute("anahtar")) + "'>";
    }

    public static String form(HttpSession s, String yol, String baslik, String alanlar, String dugme) {
        return "<section class='card'><div class='card-heading'><h2>" + metin(baslik) + "</h2></div>"
                + "<form class='fields' method='post' action='" + yol + "'>" + token(s) + alanlar
                + "<div class='form-action'><button>" + metin(dugme) + " <span aria-hidden='true'>↗</span></button></div></form></section>";
    }

    public static String tablo(List<Map<String, Object>> sonuc) {
        if (sonuc.isEmpty()) return "<div class='empty'><span class='empty-icon'>＋</span><h3>Henüz kayıt bulunmuyor</h3><p>Eklediğiniz kayıtlar burada görünecek.</p></div>";
        StringBuilder t = new StringBuilder("<div class='kaydir'><table><thead><tr>");
        sonuc.get(0).keySet().forEach(k -> t.append("<th>").append(metin(k)).append("</th>"));
        t.append("</tr></thead><tbody>");
        for (var r : sonuc) {
            t.append("<tr>");
            for (Object v : r.values()) {
                String deger = String.valueOf(v);
                if (Set.of("Bekliyor", "Onaylandı", "Reddedildi").contains(deger)) {
                    String sinif = deger.equals("Onaylandı") ? "ok" : deger.equals("Reddedildi") ? "red" : "wait";
                    t.append("<td><span class='badge ").append(sinif).append("'>").append(metin(v)).append("</span></td>");
                } else {
                    t.append("<td>").append(metin(v)).append("</td>");
                }
            }
            t.append("</tr>");
        }
        return t + "</tbody></table></div>";
    }

    public static String belge(String govde) {
        return "<!doctype html><html lang='tr'><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>Klinik · Hastane randevu sistemi</title><link rel='stylesheet' href='/stil.css'></head><body>" + govde + "</body></html>";
    }

    public static String marka() {
        return "<a class='brand' href='/'><span class='brand-icon'>✚</span><span>klinik<span class='brand-dot'>.</span><small>RANDEVU YÖNETİMİ</small></span></a>";
    }

    public static String cerceve(String icerik) {
        return belge("<div class='public-head'>" + marka() + "<a class='text-link' href='/'>Ana sayfaya dön ↗</a></div><main class='standalone'>" + icerik + "</main>");
    }

    public static String panel(HttpSession s, String aktif, String baslik, String aciklama, String icerik) {
        String[][] menu = rol(s, "Yönetici")
                ? new String[][] {{"ozet","Genel bakış"},{"subeler","Şubeler & bölümler"},{"hizmetler","Sağlık hizmetleri"},{"calisanlar","Çalışanlar"},{"mesailer","Çalışma takvimi"},{"randevular","Randevular"},{"yoneticiler","Yönetici hesapları"}}
                : rol(s, "Hasta")
                ? new String[][] {{"ozet","Genel bakış"},{"randevu","Randevu al"},{"randevular","Randevularım"}}
                : new String[][] {{"ozet","Genel bakış"},{"randevular","Randevularım"},{"kararlar","Bekleyen talepler"}};
        StringBuilder nav = new StringBuilder();
        for (String[] link : menu) {
            nav.append("<a class='nav-link ").append(link[0].equals(aktif) ? "active" : "").append("' href='/?sayfa=")
                    .append(link[0]).append("'><span class='nav-mark'></span>").append(link[1]).append("</a>");
        }
        Object ad = s.getAttribute("ad");
        String harf = ad == null || ad.toString().isBlank() ? "K" : ad.toString().substring(0, 1);
        return belge("<div class='app'><aside class='sidebar'>" + marka() + "<div class='nav-label'>ÇALIŞMA ALANI</div><nav>" + nav
                + "</nav><div class='sidebar-bottom'><div class='profile'><span class='avatar'>" + metin(harf)
                + "</span><div><strong>" + metin(ad) + "</strong><small>" + metin(s.getAttribute("rol"))
                + "</small></div></div><form method='post' action='/cikis'>" + token(s)
                + "<button class='logout'>Oturumu kapat <span>↗</span></button></form></div></aside>"
                + "<div class='workspace'><header class='topbar'><span>Çalışma alanı <b>/ " + metin(baslik) + "</b></span><span class='online'><i></i> Oturum açık</span></header>"
                + "<main class='content'><div class='page-heading'><div><p class='eyebrow'>" + metin(s.getAttribute("rol"))
                + " PANELİ</p><h1>" + metin(baslik) + "</h1><p>" + metin(aciklama) + "</p></div><div class='date-chip'>"
                + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.forLanguageTag("tr")))
                + "</div></div>" + icerik + "<footer>Klinik · Sağlık hizmetleri için randevu yönetimi</footer></main></div></div>");
    }
}