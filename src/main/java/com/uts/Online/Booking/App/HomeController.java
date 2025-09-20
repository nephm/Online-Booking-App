package com.uts.Online.Booking.App;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class HomeController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    @GetMapping("/court")
    public String court() {
        return "court";
    }
}
