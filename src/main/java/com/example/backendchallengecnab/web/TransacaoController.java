package com.example.backendchallengecnab.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backendchallengecnab.entity.TransacaoReport;
import com.example.backendchallengecnab.service.TransacaoService;

@RestController
@RequestMapping("transacoes")
public class TransacaoController {
    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    List<TransacaoReport> listAll() {
        return transacaoService.getTotaisTransacoesByNomeDaLoja();
    }
}