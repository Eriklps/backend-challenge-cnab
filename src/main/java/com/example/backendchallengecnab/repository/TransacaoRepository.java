package com.example.backendchallengecnab.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.backendchallengecnab.entity.Transacao;

public interface TransacaoRepository extends CrudRepository<Transacao, Long> {
    List<Transacao> findAllByOrderByNomeDaLojaAscIdDesc();
}