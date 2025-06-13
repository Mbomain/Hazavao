package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.HazavaoResponse;
import com.example.demo.service.HazavaoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class HazavaoController {
    private HazavaoService hazavaoService;

    @GetMapping("/hazavao")
    public HazavaoResponse hazavao(@RequestParam String teny) {
        return hazavaoService.getDefinition(teny);
    }
}