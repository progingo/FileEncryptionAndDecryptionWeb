package org.progingo.fileencryptionanddecryptionweb.controller;

import org.progingo.fileencryptionanddecryptionweb.service.PasswordBookService;
import org.progingo.fileencryptionanddecryptionweb.util.Respond;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pb")
public class PasswordBookController {

    @Autowired
    private PasswordBookService passwordBookService;


    @PostMapping("/add")
    public Respond addPasswordBook(String zh, String pas, String workpath){

        return passwordBookService.add(zh,pas,workpath);
    }

}
