package tr.ndp.hastane;

import java.time.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"hastane.menu=false","spring.datasource.url=jdbc:h2:mem:sinav"})
class RandevuTest {
    @Autowired JdbcTemplate db;
    @Autowired Rezervasyon servis;
    @Autowired Takvim takvim;

    @Test
    void eszamanliTaleplerVeSinirSaatleri() throws Exception {
        db.update("insert into kurum(ad,ac,kapa) values('Merkez','09:00','17:00')");
        db.update("insert into bolum(kurum,ad) values(1,'Dahiliye')");
        db.update("insert into hizmet(bolum,ad,sure,ucret) values(1,'Muayene',30,125.50)");
        db.update("insert into kisi(hesap,ad,parola,rol,bolum) values('d','Doktor','x','Doktor',1)");
        db.update("insert into kisi(hesap,ad,parola,rol) values('a','Ali','x','Hasta'),('b','Ece','x','Hasta')");
        db.update("insert into sunum values(1,1)");
        LocalDate gun = LocalDate.now().plusDays(1);
        db.update("insert into mesai values(1,?,'09:00','10:00')",gun);
        var secim = new Takvim.Secim(1,1,gun);
        var ilk = LocalTime.of(9,0);
        var hazir = new CyclicBarrier(2);
        var havuz = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> talep = () -> {
                hazir.await(5,TimeUnit.SECONDS);
                try {
                    servis.ayir(2,secim,ilk);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            };
            var a = havuz.submit(talep);
            var b = havuz.submit(talep);
            assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
        } finally {
            havuz.shutdownNow();
        }
        assertEquals(1,db.queryForObject("select count(*) from randevu",Integer.class));
        assertEquals(java.util.Set.of(ilk.plusMinutes(30)),takvim.hesapla(3,secim).saatler());
        db.update("update randevu set durum='Reddedildi'");
        assertTrue(takvim.hesapla(3,secim).saatler().contains(ilk));
    }
}
