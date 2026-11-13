package com.doacoes.shared.erro;

import java.util.Map;
import org.hibernate.query.sqm.UnknownPathException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Converte exceções em respostas no formato Problem Details (RFC 9457). */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    ProblemDetail regraDeNegocio(RegraNegocioException ex) {
        return problema(HttpStatusCode.valueOf(422), "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    ProblemDetail conflito(ConflitoException ex) {
        ProblemDetail problema = problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
        ex.detalhes().forEach(problema::setProperty);
        return problema;
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    ProblemDetail concorrencia(Exception ex) {
        return problema(HttpStatus.CONFLICT, "Conflito",
                "O registro foi alterado por outra operação ao mesmo tempo. Tente novamente.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", "A operação viola uma restrição de integridade dos dados.");
    }

    /** O parâmetro "sort" citou um campo que a entidade não tem. Qualquer outro mau uso segue como erro 500. */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    ProblemDetail ordenacaoInvalida(InvalidDataAccessApiUsageException ex) {
        if (ex.contains(UnknownPathException.class)) {
            return problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido", "Campo de ordenação desconhecido.");
        }
        throw ex;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problema = ex.getBody();
        problema.setTitle("Dados inválidos");
        problema.setDetail("Um ou mais campos estão inválidos.");
        problema.setProperty("erros", ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> Map.of("campo", erro.getField(), "mensagem", String.valueOf(erro.getDefaultMessage())))
                .toList());
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    /** JSON malformado ou valor fora do permitido (ex.: um tipo que não existe no enum). */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problema = problema(status, "Dados inválidos",
                "O corpo da requisição não pôde ser lido. Confira o JSON e os valores permitidos em cada campo.");
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    private static ProblemDetail problema(HttpStatusCode status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
