package com.example.loadgenerator.controller;

import com.example.loadgenerator.config.LoadGeneratorConfig;
import com.example.loadgenerator.service.LoadTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/loadtest")
@RequiredArgsConstructor
public class LoadTestController {

    private final LoadTestService loadTestService;
    private final LoadGeneratorConfig config;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", config.isEnabled());
        status.put("targetUrl", config.getTargetUrl());
        status.put("threads", config.getThreads());
        status.put("requestsPerSecond", config.getRequestsPerSecond());
        status.put("serviceAvailable", loadTestService.isTargetServiceAvailable());
        return status;
    }

    @PostMapping("/config/rps")
    public Map<String, Object> updateRps(@RequestParam int rps) {
        config.setRequestsPerSecond(rps);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "RPS обновлен");
        response.put("newRps", rps);
        return response;
    }

    @PostMapping("/stats/reset")
    public Map<String, String> resetStats() {
        loadTestService.resetStatistics();
        return Map.of("message", "Статистика сброшена");
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        // Здесь можно добавить метод для получения статистики
        return Map.of("message", "Используйте логи для просмотра статистики");
    }
}