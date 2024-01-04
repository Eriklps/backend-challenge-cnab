package com.example.backendchallengecnab.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.backendchallengecnab.entity.Transacao;
import com.example.backendchallengecnab.entity.TransacaoReport;
import com.example.backendchallengecnab.repository.TransacaoRepository;

@Service
public class TransacaoService {
    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    public List<TransacaoReport> getTotaisTransacoesByNomeDaLoja() {
        List<Transacao> transacoes = repository.findAllByOrderByNomeDaLojaAscIdDesc();

        // Preserves Order
        Map<String, TransacaoReport> reportMap = new LinkedHashMap<>();

        transacoes.forEach(transacao -> {
            var nomeDaLoja = transacao.nomeDaLoja();
            var valor = transacao.valor();

            reportMap.compute(nomeDaLoja, (key, existingReport) -> {
                TransacaoReport report = (existingReport != null) ? existingReport
                        : new TransacaoReport(BigDecimal.ZERO, key, new ArrayList<>());
                return report.addTotal(valor).addTransacao(transacao);
            });
        });

        return new ArrayList<>(reportMap.values());
    }
}