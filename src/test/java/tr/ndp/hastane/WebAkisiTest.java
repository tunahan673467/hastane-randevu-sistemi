package tr.ndp.hastane;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:webakisi")
@AutoConfigureMockMvc
class WebAkisiTest {
    @Autowired MockMvc mvc;

    void yaz(MockHttpSession s,String yol,String... veri) throws Exception {
        var istek=post(yol).session(s).param("anahtar",(String)s.getAttribute("anahtar"));
        for(int i=0;i<veri.length;i+=2) istek.param(veri[i],veri[i+1]);
        mvc.perform(istek).andExpect(status().is3xxRedirection());
    }

    MockHttpSession oturum() throws Exception {
        var s=new MockHttpSession();
        mvc.perform(get("/").session(s)).andExpect(status().isOk());
        return s;
    }

    @Test
    void ilkKurulumdanOnaya() throws Exception {
        var y=oturum();
        mvc.perform(post("/kurulum").session(y)).andExpect(status().isForbidden());
        yaz(y,"/kurulum","ad","Yönetici","hesap","yon","parola","deneme123");
        yaz(y,"/giris","hesap","yon","parola","deneme123");
        yaz(y,"/kurum","ad","Merkez","ac","09:00","kapa","17:00");
        yaz(y,"/bolum","kurum","1","ad","Dahiliye");
        yaz(y,"/hizmet","bolum","1","ad","Muayene","sure","30","ucret","100.50");
        yaz(y,"/calisan","ad","Doktor","hesap","dr","parola","deneme123","bolum","1","uzmanlik","Dahiliye","tur","Doktor");
        yaz(y,"/ata","kisi","2","hizmet","1");
        String gun=LocalDate.now().plusDays(1).toString();
        yaz(y,"/mesai","kisi","2","gun",gun,"ilk","09:00","son","10:00");
        var h=oturum();
        yaz(h,"/hasta","ad","Hasta","hesap","hasta","parola","deneme123");
        yaz(h,"/giris","hesap","hasta","parola","deneme123");
        mvc.perform(get("/").session(h)).andExpect(content().string(containsString("Merkez / Dahiliye")));
        mvc.perform(get("/saatler").session(h).param("secim","2-1").param("gun",gun))
                .andExpect(content().string(containsString("<option>09:00</option>")));
        yaz(h,"/ayir","kisi","2","hizmet","1","gun",gun,"ilk","09:00");
        mvc.perform(post("/karar").session(h).param("anahtar",(String)h.getAttribute("anahtar")).param("id","1").param("karar","Onaylandı")).andExpect(status().isBadRequest());
        var dr=oturum();
        yaz(dr,"/giris","hesap","dr","parola","deneme123");
        mvc.perform(get("/").session(dr)).andExpect(content().string(containsString("Talep değerlendir")));
        yaz(dr,"/karar","id","1","karar","Onaylandı");
        mvc.perform(get("/").session(h)).andExpect(content().string(containsString("Onaylandı")));
    }
}
