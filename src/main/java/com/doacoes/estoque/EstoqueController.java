package com.doacoes.estoque;

import com.doacoes.shared.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/estoque")
@Tag(name = "Estoque")
public class EstoqueController {

    private final EstoqueService service;

    public EstoqueController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Saldo por alimento: disponível para distribuição e vencido aguardando descarte")
    public List<SaldoAlimento> saldos() {
        return service.saldos();
    }

    @GetMapping("/lotes")
    @Operation(summary = "Lotes com saldo, do que vence antes para o que vence depois")
    public List<LoteResponse> lotes(
            @RequestParam(required = false) Long alimentoId,
            @RequestParam(required = false) Boolean vencidos,
            @RequestParam(required = false) Integer venceEmDias) {
        return service.lotes(alimentoId, vencidos, venceEmDias);
    }

    @PostMapping("/lotes/{id}/descarte")
    @Operation(summary = "Dá baixa em um lote por vencimento, avaria ou perda")
    public LoteResponse descartar(@PathVariable Long id, @Valid @RequestBody DescarteRequest dados) {
        return service.descartar(id, dados);
    }

    @GetMapping("/movimentacoes")
    @Operation(summary = "Livro de estoque: todas as entradas, saídas, descartes e estornos")
    public Pagina<MovimentacaoResponse> movimentacoes(
            @RequestParam(required = false) Long alimentoId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) LocalDate de,
            @RequestParam(required = false) LocalDate ate,
            @ParameterObject
            @PageableDefault(size = 20, sort = {"criadoEm", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.movimentacoes(alimentoId, tipo, de, ate, pageable);
    }
}
