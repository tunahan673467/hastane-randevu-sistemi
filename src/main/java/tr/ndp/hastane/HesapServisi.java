package tr.ndp.hastane;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HesapServisi {
    private final JdbcTemplate db;
    private final BCryptPasswordEncoder sifre = new BCryptPasswordEncoder();

    public HesapServisi(JdbcTemplate db) {
        this.db = db;
    }

    public boolean kurulum() {
        return db.queryForObject("select count(*) from kisi where rol='Yönetici'", Integer.class) == 0;
    }

    @Transactional
    public void ekle(String ad, String hesap, String parola, String rol, Long bolum, String uzmanlik) {
        if (rol.equals("Yönetici")) {
            db.queryForObject("select id from kilit where id=1 for update", Integer.class);
            if (!kurulum()) throw new IllegalArgumentException("Kurulum tamamlandı. Yeni yönetici eklemek için yönetici hesabınızla giriş yapın.");
        }
        kaydet(ad, hesap, parola, rol, bolum, uzmanlik);
    }

    @Transactional
    public void yoneticiEkle(long yetkili, String ad, String hesap, String parola) {
        int sayi = db.queryForObject("select count(*) from kisi where id=? and rol='Yönetici'", Integer.class, yetkili);
        if (sayi != 1) throw new IllegalArgumentException("Yönetici hesabı oluşturma yetkiniz yok.");
        kaydet(ad, hesap, parola, "Yönetici", null, null);
    }

    private void kaydet(String ad, String hesap, String parola, String rol, Long bolum, String uzmanlik) {
        if (ad.isBlank() || hesap.isBlank() || parola.length() < 8 || parola.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Ad ve kullanıcı adı girin. Parola en az 8 karakter ve en fazla 72 bayt olmalı.");
        db.update("insert into kisi(ad,hesap,parola,rol,bolum,uzmanlik) values(?,?,?,?,?,?)",
                ad.strip(), hesap.strip().toLowerCase(Locale.ROOT), sifre.encode(parola), rol, bolum, uzmanlik);
    }

    public record Kimlik(long id, String ad, String rol) {}

    public Kimlik gir(String hesap, String parola) {
        var sonuc = db.queryForList("select id,ad,rol,parola from kisi where hesap=?", hesap.strip().toLowerCase(Locale.ROOT));
        if (sonuc.isEmpty() || !sifre.matches(parola, (String) sonuc.get(0).get("parola")))
            throw new IllegalArgumentException("Kullanıcı adı veya parola hatalı.");
        var r = sonuc.get(0);
        return new Kimlik(((Number) r.get("ID")).longValue(), (String) r.get("ad"), (String) r.get("rol"));
    }
}