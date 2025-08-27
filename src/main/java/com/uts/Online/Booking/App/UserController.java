package com.uts.Online.Booking.App;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
    @RequestMapping("/index")
    public String index(){
        return "index.html"; 
    }
}
 