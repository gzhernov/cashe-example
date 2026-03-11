//package com.example.app.service;
//
//import com.example.core.model.ClientProfile;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//public class ClientProfileService {
//
//    @Cacheable(value = "clientProfiles", key = "#clientId")
//    public ClientProfile getClientProfile(Long clientId) {
//        // Имитация долгой работы (5 секунд)
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        return ClientProfile.builder()
//                .id(clientId)
//                .name("Клиент " + clientId)
//                .email("client" + clientId + "@example.com")
//                .phone("+7-999-123-45-6" + clientId)
//                .company("Компания " + clientId)
//                .position("Менеджер")
//                .registrationDate(LocalDateTime.now().minusMonths(clientId % 12))
//                .status(clientId % 3 == 0 ? "ACTIVE" : "PENDING")
//                .build();
//    }
//}