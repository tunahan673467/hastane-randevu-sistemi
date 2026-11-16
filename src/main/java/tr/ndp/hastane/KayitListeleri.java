package tr.ndp.hastane;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import static tr.ndp.hastane.Sayfa.*;

@Component
public record KayitListeleri(JdbcTemplate db) {
    public String kayitlar(String baslik, String sql) {
        return "<section class='card'><div class='card-heading'><h2>" + baslik + "</h2><span class='muted'>Kayıtlar</span></div>"
                + tablo(db.queryForList(sql)) + "</section>";
    }

    public String goster(String sayfa) {
        return switch (sayfa) {
            case "subeler" -> kayitlar("Kayıtlı şubeler", "select ad şube,ac açılış,kapa kapanış from kurum order by ad")
                    + kayitlar("Kayıtlı bölümler", "select k.ad şube,b.ad bölüm from bolum b join kurum k on k.id=b.kurum order by k.ad,b.ad");
            case "hizmetler" -> kayitlar("Hizmet kataloğu", "select k.ad şube,b.ad bölüm,h.ad hizmet,h.sure dakika,h.ucret ücret from hizmet h join bolum b on b.id=h.bolum join kurum k on k.id=b.kurum order by k.ad,b.ad,h.ad");
            case "calisanlar" -> kayitlar("Ekip listesi", "select p.ad çalışan,p.hesap kullanıcı,p.rol görev,p.uzmanlik uzmanlık,k.ad şube,b.ad bölüm from kisi p join bolum b on b.id=p.bolum join kurum k on k.id=b.kurum order by p.ad")
                    + kayitlar("Hizmet atamaları", "select p.ad çalışan,h.ad hizmet from sunum s join kisi p on p.id=s.kisi join hizmet h on h.id=s.hizmet order by p.ad,h.ad");
            case "mesailer" -> kayitlar("Tanımlı çalışma aralıkları", "select p.ad çalışan,m.gun gün,m.ilk başlangıç,m.son bitiş from mesai m join kisi p on p.id=m.kisi order by m.gun desc,p.ad");
            default -> "";
        };
    }

}
