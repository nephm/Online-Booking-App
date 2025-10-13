package com.uts.Online.Booking.App;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class HomeController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    @GetMapping("/venues")
    public String getVenues() {
        return "venues";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/roles")
    public String roles() {
        return "roles";
    }
}
