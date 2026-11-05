package tr.ndp.hastane;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import static tr.ndp.hastane.Sayfa.sec;

@Component
public record Katalog(JdbcTemplate db) {
    public String kurum() { return sec("kurum","Şube",db.queryForList("select id,ad from kurum order by ad")); }
    public String bolum() { return sec("bolum","Bölüm",db.queryForList("select b.id,k.ad || ' / ' || b.ad ad from bolum b join kurum k on k.id=b.kurum order by ad")); }
    public String hizmet() { return sec("hizmet","Hizmet",db.queryForList("select h.id,k.ad || ' / ' || b.ad || ' / ' || h.ad ad from hizmet h join bolum b on b.id=h.bolum join kurum k on k.id=b.kurum order by ad")); }
    public String personel() { return sec("kisi","Çalışan",db.queryForList("select p.id,k.ad || ' / ' || b.ad || ' / ' || p.ad ad from kisi p join bolum b on b.id=p.bolum join kurum k on k.id=b.kurum order by ad")); }
}
