package tr.ndp.hastane;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:yoneticiler")
@AutoConfigureMockMvc
class YoneticiOturumTest {
    @Autowired MockMvc mvc;

    MockHttpSession yeniOturum() throws Exception {
        var s = new MockHttpSession();
        mvc.perform(get("/").session(s)).andExpect(status().isOk())
                .andExpect(content().string(containsString(Sayfa.metin("Yönetici girişi"))));
        return s;
    }

    void yaz(MockHttpSession s, String yol, String... alanlar) throws Exception {
        var istek = post(yol).session(s).param("anahtar", (String) s.getAttribute("anahtar"));
        for (int i = 0; i < alanlar.length; i += 2) istek.param(alanlar[i], alanlar[i + 1]);
        mvc.perform(istek).andExpect(status().is3xxRedirection());
    }

    @Test
    void ikiYoneticiArasindaGecisVeYetki() throws Exception {
        var ilk = yeniOturum();
        yaz(ilk, "/kurulum", "ad", "Birinci Yönetici", "hesap", "ilk", "parola", "deneme123");
        yaz(ilk, "/giris", "hesap", "ilk", "parola", "deneme123", "beklenen", "Yönetici");
        yaz(ilk, "/yonetici", "ad", "İkinci Yönetici", "hesap", "ikinci", "parola", "deneme456");
        mvc.perform(get("/").session(ilk).param("sayfa", "yoneticiler"))
                .andExpect(content().string(containsString(Sayfa.metin("İkinci Yönetici"))));
        mvc.perform(post("/cikis").session(ilk).param("anahtar", (String) ilk.getAttribute("anahtar")))
                .andExpect(redirectedUrl("/?giris=yonetici"));
        assertTrue(ilk.isInvalid());
        var ikinci = yeniOturum();
        mvc.perform(get("/").session(ikinci).param("giris", "kurulum"))
                .andExpect(content().string(containsString("yetkiliHesap")));
        mvc.perform(post("/kurulum").session(ikinci).param("anahtar", (String) ikinci.getAttribute("anahtar"))
                .param("ad", "Yetkisiz").param("hesap", "izinsiz").param("parola", "deneme789"))
                .andExpect(status().isBadRequest());
        yaz(ikinci, "/kurulum", "ad", "Üçüncü Yönetici", "hesap", "ucuncu", "parola", "deneme789", "yetkiliHesap", "ilk", "yetkiliParola", "deneme123");
        yaz(ikinci, "/giris", "hesap", "ikinci", "parola", "deneme456", "beklenen", "Yönetici");
        mvc.perform(get("/").session(ikinci))
                .andExpect(content().string(containsString(Sayfa.metin("Hoş geldiniz, İkinci Yönetici"))));
        var hasta = yeniOturum();
        yaz(hasta, "/hasta", "ad", "Hasta", "hesap", "hasta", "parola", "deneme123");
        mvc.perform(post("/giris").session(hasta).param("anahtar", (String) hasta.getAttribute("anahtar"))
                .param("hesap", "hasta").param("parola", "deneme123").param("beklenen", "Yönetici"))
                .andExpect(status().isBadRequest());
        yaz(hasta, "/giris", "hesap", "hasta", "parola", "deneme123", "beklenen", "Hasta");
        mvc.perform(post("/yonetici").session(hasta).param("anahtar", (String) hasta.getAttribute("anahtar"))
                .param("ad", "Yetkisiz").param("hesap", "kotu").param("parola", "deneme123"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/").session(hasta).param("sayfa", "yoneticiler")).andExpect(status().isBadRequest());
    }
}
