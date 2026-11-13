package com.doacoes.cadastro.instituicao;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instituicao")
@Tag(name = "Instituição")
public class InstituicaoController {

    private final InstituicaoService service;

    public InstituicaoController(InstituicaoService service) {
        this.service = service;
    }

    @GetMapping
    public InstituicaoResponse detalhar() {
        return service.detalhar();
    }

    @PutMapping
    public InstituicaoResponse atualizar(@Valid @RequestBody InstituicaoRequest dados) {
        return service.atualizar(dados);
    }
}
