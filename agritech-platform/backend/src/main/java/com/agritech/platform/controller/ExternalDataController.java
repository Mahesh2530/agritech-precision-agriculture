package com.agritech.platform.controller;

import com.agritech.platform.service.external.NwdpWindService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExternalDataController {

    private final NwdpWindService nwdpWindService;

    @GetMapping("/external/wind-speed")
    public Map<String, Object> getWindSpeed(
            @RequestParam(defaultValue = "Andhra Pradesh") String state,
            @RequestParam(defaultValue = "KRISHNA") String district,
            @RequestParam(defaultValue = "Andhra Pradesh GW") String agency,
            @RequestParam(defaultValue = "JSON") String format) {
        return nwdpWindService.fetchWindData(state, district, agency, format);
    }
}
