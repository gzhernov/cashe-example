//package com.example.app.controller;
//
//import com.example.app.service.ClientProfileService;
//import com.example.core.model.ClientProfile;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/clients")
//public class ClientController {
//
//    private final ClientProfileService clientService;
//
//    public ClientController(ClientProfileService clientService) {
//        this.clientService = clientService;
//    }
//
//    @GetMapping("/{clientId}/profile")
//    public ClientProfile getClientProfile(@PathVariable Long clientId) {
//        long startTime = System.currentTimeMillis();
//
//        ClientProfile profile = clientService.getClientProfile(clientId);
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
//
//        return profile;
//    }
//}