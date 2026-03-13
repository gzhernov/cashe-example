package com.example.app.controller.stats;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Profile("!caffeine & !redis & !memcached")
@RestController
@RequestMapping("/api/cache/stats")
public class DefaultCacheStatsController {
    // Базовый контроллер
}
