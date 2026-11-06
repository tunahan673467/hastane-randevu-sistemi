package tr.ndp.hastane;

import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class Hatalar {
    @ExceptionHandler({IllegalArgumentException.class,DataAccessException.class,java.time.DateTimeException.class,ArithmeticException.class})
    ResponseEntity<String> hata(RuntimeException e) {
        String mesaj=e instanceof DataAccessException ? "Kayıt yapılamadı. Seçimleri, saatleri ve yinelenen kayıtları kontrol edin." : e.getMessage();
        return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/html;charset=UTF-8"))
                .body(Sayfa.cerceve("<section><h2>İşlem tamamlanamadı</h2><p>"+Sayfa.metin(mesaj)+"</p><a href='/'>Panele dön</a></section>"));
    }
}
