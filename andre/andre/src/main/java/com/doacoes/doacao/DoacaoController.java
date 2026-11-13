package com.doacoes.doacao;

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
@RequestMapping("/api/v1/doacoes")
@Tag(name = "Doações")
public class DoacaoController {

    private final DoacaoService service;

    public DoacaoController(DoacaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registra uma doação; cada item vira um lote no estoque")
    public ResponseEntity<DoacaoResponse> registrar(@Valid @RequestBody DoacaoRequest dados) {
        DoacaoResponse criada = service.registrar(dados);
        return ResponseEntity.created(URI.create("/api/v1/doacoes/" + criada.id())).body(criada);
    }

    @GetMapping
    @Operation(summary = "Histórico de doações, com filtro por doador e por período de recebimento")
    public Pagina<DoacaoResponse> listar(
            @RequestParam(required = false) Long doadorId,
            @RequestParam(required = false) LocalDate de,
            @RequestParam(required = false) LocalDate ate,
            @ParameterObject
            @PageableDefault(size = 20, sort = {"dataRecebimento", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.listar(doadorId, de, ate, pageable);
    }

    @GetMapping("/{id}")
    public DoacaoResponse detalhar(@PathVariable Long id) {
        return service.detalhar(id);
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela a doação; só é possível enquanto nenhum dos seus lotes teve saída")
    public DoacaoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelamentoRequest dados) {
        return service.cancelar(id, dados.motivo());
    }
}
