package com.donatech.users.controller;

import com.donatech.users.model.Comuna;
import com.donatech.users.model.Region;
import com.donatech.users.service.ComunaService;
import com.donatech.users.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LocationController {

    private final RegionService regionService;
    private final ComunaService comunaService;

    public LocationController(RegionService regionService, ComunaService comunaService) {
        this.regionService = regionService;
        this.comunaService = comunaService;
    }

    @GetMapping("/regions")
    public ResponseEntity<List<Region>> getAllRegions() {
        return ResponseEntity.ok(regionService.getAll());
    }

    @GetMapping("/comunas/by-region/{regionId}")
    public ResponseEntity<List<Comuna>> getComunasByRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(comunaService.getByRegion(regionId));
    }
}
