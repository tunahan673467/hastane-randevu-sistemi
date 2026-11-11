package tr.ndp.hastane;

import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public record Takvim(JdbcTemplate db) {
    public record Secim(long personel, long hizmet, LocalDate gun) {}
    record Aralik(LocalTime ilk, LocalTime son) {}
    public record Teklif(SortedSet<LocalTime> saatler, int sure, java.math.BigDecimal ucret) {}

    public Teklif hesapla(long hasta, Secim secim) {
        var h = db.queryForMap("""
                select h.sure,h.ucret from hizmet h join sunum s on h.id=s.hizmet
                where s.kisi=? and s.hizmet=?
                """, secim.personel(), secim.hizmet());
        int sure = ((Number)h.get("sure")).intValue();
        var dolu = db.query("""
                select ilk,son from randevu where gun=? and (hasta=? or personel=?)
                and durum in ('Bekliyor','Onaylandı')
                """, (rs,i) -> new Aralik(rs.getTime(1).toLocalTime(), rs.getTime(2).toLocalTime()),
                secim.gun(), hasta, secim.personel());
        var mesai = db.query("select ilk,son from mesai where gun=? and kisi=?",
                (rs,i) -> new Aralik(rs.getTime(1).toLocalTime(), rs.getTime(2).toLocalTime()),
                secim.gun(),secim.personel());
        SortedSet<LocalTime> saatler = new TreeSet<>();
        var simdi = LocalDateTime.now();
        for (Aralik m : mesai) {
            for (int dk=m.ilk().toSecondOfDay()/60; dk+sure<=m.son().toSecondOfDay()/60; dk+=5) {
                var ilk = LocalTime.ofSecondOfDay(dk*60L);
                var son = ilk.plusMinutes(sure);
                boolean bos = dolu.stream().noneMatch(a -> ilk.isBefore(a.son()) && son.isAfter(a.ilk()));
                if (bos && secim.gun().atTime(ilk).isAfter(simdi)) saatler.add(ilk);
            }
        }
        return new Teklif(saatler,sure,(java.math.BigDecimal)h.get("ucret"));
    }
}
