package tr.ndp.hastane;

import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Rezervasyon {
    private final Takvim takvim;

    public Rezervasyon(Takvim takvim) { this.takvim = takvim; }

    @Transactional
    public void ayir(long hasta, Takvim.Secim secim, LocalTime ilk) {
        var db = takvim.db();
        db.queryForObject("select id from kilit where id=1 for update", Integer.class);
        var teklif = takvim.hesapla(hasta, secim);
        if (!teklif.saatler().contains(ilk)) throw new IllegalArgumentException("Bu saat uygun değil.");
        db.update("""
                insert into randevu(hasta,personel,hizmet,gun,ilk,son,ucret,durum)
                values(?,?,?,?,?,?,?,'Bekliyor')
                """, hasta,secim.personel(),secim.hizmet(),secim.gun(),ilk,ilk.plusMinutes(teklif.sure()),teklif.ucret());
    }
}
