package com.ronanos.backend.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

// Forwards app routes to index.html so React Router handles them.
// Static resources and API routes must be excluded to avoid forward loops.
@Controller
public class SpaController {

    @RequestMapping(value = {
            "/{path:^(?!api|assets|favicon|index\\.html$).*}",
            "/{path:^(?!api|assets|favicon|index\\.html$).*}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
