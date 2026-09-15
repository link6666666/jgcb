package com.jgcb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/login", "/register", "/profile", "/members", "/travel", "/memoirs",
        "/mahjong", "/billiard", "/boardgame", "/karaoke", "/movie",
        "/checkin", "/pet", "/room"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
