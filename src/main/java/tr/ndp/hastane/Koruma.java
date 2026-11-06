package tr.ndp.hastane;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class Koruma extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain zincir)
            throws ServletException,IOException {
        res.setHeader("Cache-Control","no-store");
        res.setHeader("X-Frame-Options","DENY");
        if(req.getMethod().equals("POST")) {
            var s=req.getSession(false);
            String token=req.getParameter("anahtar");
            if(s==null || token==null || !token.equals(s.getAttribute("anahtar"))) {
                res.sendError(403);
                return;
            }
        }
        zincir.doFilter(req,res);
    }
}
