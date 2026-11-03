package tr.ndp.hastane;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
public record AnaSayfa(List<Bolme> bolmeler) {
    @GetMapping(value="/",produces="text/html;charset=UTF-8")
    public String ana(HttpSession s) {
        if(s.getAttribute("anahtar")==null) s.setAttribute("anahtar",UUID.randomUUID().toString());
        StringBuilder html=new StringBuilder();
        if(s.getAttribute("ad")!=null) html.append("<p class='kimlik'>").append(Sayfa.metin(s.getAttribute("ad"))).append(" · ").append(Sayfa.metin(s.getAttribute("rol"))).append("</p>");
        for(Bolme bolme:bolmeler) html.append(bolme.goster(s));
        return Sayfa.cerceve(html.toString());
    }
}
