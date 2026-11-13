package com.doacoes.cadastro;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pessoas")
@Tag(name = "Pessoas")
public class PessoaController {

    private final PessoaService service;

    public PessoaController(PessoaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Busca por nome em doadores e beneficiários ao mesmo tempo")
    public List<PessoaResumo> buscar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo) {
        return service.buscar(busca, ativo);
    }
}
