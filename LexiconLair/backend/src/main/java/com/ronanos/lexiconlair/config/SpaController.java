package com.ronanos.lexiconlair.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
