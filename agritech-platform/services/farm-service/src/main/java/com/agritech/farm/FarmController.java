package com.agritech.farm;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FarmController {

    private final List<Map<String, Object>> farms = new ArrayList<>();

    @GetMapping("/farms")
    public List<Map<String, Object>> listFarms() {
        return farms;
    }

    @PostMapping("/farms")
    public ResponseEntity<Map<String, Object>> createFarm(@RequestBody FarmRequest request) {
        Map<String, Object> farm = Map.of(
                "id", farms.size() + 1,
                "name", request.name(),
                "location", request.location(),
                "latitude", request.latitude(),
                "longitude", request.longitude()
        );
        farms.add(farm);
        return ResponseEntity.ok(farm);
    }

    public record FarmRequest(String name, String location, Double latitude, Double longitude) {}
}
