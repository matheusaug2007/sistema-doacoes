package com.doacoes.distribuicao;

import com.doacoes.shared.CancelamentoRequest;
import com.doacoes.shared.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/distribuicoes")
@Tag(name = "Distribuições")
public class DistribuicaoController {

    private final DistribuicaoService service;

    public DistribuicaoController(DistribuicaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registra uma entrega; o sistema escolhe os lotes pelo vencimento mais próximo (FEFO)")
    public ResponseEntity<DistribuicaoResponse> registrar(@Valid @RequestBody DistribuicaoRequest dados) {
        DistribuicaoResponse criada = service.registrar(dados);
        return ResponseEntity.created(URI.create("/api/v1/distribuicoes/" + criada.id())).body(criada);
    }

    @GetMapping
    @Operation(summary = "Histórico de distribuições, com filtro por beneficiário e por período")
    public Pagina<DistribuicaoResponse> listar(
            @RequestParam(required = false) Long beneficiarioId,
            @RequestParam(required = false) LocalDate de,
            @RequestParam(required = false) LocalDate ate,
            @ParameterObject
            @PageableDefault(size = 20, sort = {"dataDistribuicao", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.listar(beneficiarioId, de, ate, pageable);
    }

    @GetMapping("/{id}")
    public DistribuicaoResponse detalhar(@PathVariable Long id) {
        return service.detalhar(id);
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela a distribuição e devolve as quantidades aos lotes de origem")
    public DistribuicaoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelamentoRequest dados) {
        return service.cancelar(id, dados.motivo());
    }
}
