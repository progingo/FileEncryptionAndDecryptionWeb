package org.progingo.fileencryptionanddecryptionweb.controller;


import org.progingo.fileencryptionanddecryptionweb.domain.PasswordData;
import org.progingo.fileencryptionanddecryptionweb.service.PasswordDataService;
import org.progingo.fileencryptionanddecryptionweb.util.Respond;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pd")
public class PasswordDataController {

    @Autowired
    private PasswordDataService passwordDataService;

    @PostMapping("/init")
    public Respond login(String zh,String pas,String workpath){
        Respond init = passwordDataService.init(zh, pas, workpath);
        if (init.getState() != 200)
            return init;

        return passwordDataService.initPasswordList();
    }
    @GetMapping("/reinit")
    public Respond login(){
        Respond init = passwordDataService.init();
        if (init.getState() != 200)
            return init;

        return passwordDataService.initPasswordList();
    }

    @GetMapping("/show")
    public Respond show(){
        return passwordDataService.showList();
    }

    @PostMapping("/showPass")
    public Respond showPassword(int i){
        return passwordDataService.showPassword(i);
    }

    @PostMapping("/deletePass")
    public Respond deletePassword(int i){
        return passwordDataService.deletePassword(i);
    }

    @PostMapping("/add")
    public Respond addPassword(PasswordData passwordData){
        passwordData.setState(0);
        return passwordDataService.addPassword(passwordData);
    }


}
