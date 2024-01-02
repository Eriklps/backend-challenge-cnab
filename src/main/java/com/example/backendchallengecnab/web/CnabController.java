package com.example.backendchallengecnab.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.backendchallengecnab.service.CnabService;

@RestController
@RequestMapping("cnab")
public class CnabController {
    private final CnabService cnabService;

    public CnabController(CnabService cnabService) {
        this.cnabService = cnabService;
    }

    @CrossOrigin(origins = { "http://localhost:9090", "https://frontend-pagnet.onrender.com" })
    @PostMapping("upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        cnabService.uploadCnabFile(file);
        return "Processing started!";
    }
}