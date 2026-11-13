package com.doacoes.cadastro.beneficiario;

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
@RequestMapping("/api/v1/beneficiarios")
@Tag(name = "Beneficiários")
public class BeneficiarioController {

    private final BeneficiarioService service;

    public BeneficiarioController(BeneficiarioService service) {
        this.service = service;
    }

    @GetMapping
    public Pagina<BeneficiarioResponse> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String busca,
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(ativo, busca, pageable);
    }

    @GetMapping("/{id}")
    public BeneficiarioResponse detalhar(@PathVariable Long id) {
        return service.detalhar(id);
    }

    @PostMapping
    public ResponseEntity<BeneficiarioResponse> criar(@Valid @RequestBody BeneficiarioRequest dados) {
        BeneficiarioResponse criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/v1/beneficiarios/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public BeneficiarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody BeneficiarioRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclusão lógica: o beneficiário fica inativo e o histórico de distribuições é preservado")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativacao")
    public BeneficiarioResponse reativar(@PathVariable Long id) {
        return service.reativar(id);
    }
}
