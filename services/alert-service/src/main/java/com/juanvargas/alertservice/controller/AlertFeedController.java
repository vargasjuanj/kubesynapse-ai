package com.juanvargas.alertservice.controller;

import com.juanvargas.alertservice.domain.DispatchedAlert;
import com.juanvargas.alertservice.service.AlertDeduplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertFeedController {

    private final AlertDeduplicationService alertService;

    @GetMapping
    public ResponseEntity<List<DispatchedAlert>> getAlertFeed() {
        return ResponseEntity.ok(alertService.getRecentAlerts());
    }
}
