package com.doacoes.cadastro.alimento;

import com.doacoes.shared.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alimentos")
@Tag(name = "Alimentos")
public class AlimentoController {

    private final AlimentoService service;

    public AlimentoController(AlimentoService service) {
        this.service = service;
    }

    @GetMapping
    public Pagina<AlimentoResponse> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String busca,
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(ativo, busca, pageable);
    }

    @GetMapping("/{id}")
    public AlimentoResponse detalhar(@PathVariable Long id) {
        return service.detalhar(id);
    }

    @PostMapping
    public ResponseEntity<AlimentoResponse> criar(@Valid @RequestBody AlimentoRequest dados) {
        AlimentoResponse criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/v1/alimentos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public AlimentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlimentoRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclusão lógica: o alimento deixa de aceitar novas doações; o estoque existente continua distribuível")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativacao")
    public AlimentoResponse reativar(@PathVariable Long id) {
        return service.reativar(id);
    }
}
