package io.github.darcymf.ats.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import io.github.darcymf.ats.pdf.UnreadablePdfException;

/** Turns errors into a short message the page can show as is. */
@RestControllerAdvice
public class ApiErrors {

    private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<Map<String, String>> badRequest(BadRequestException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity<Map<String, String>> noFile(MissingServletRequestPartException e) {
        return error(HttpStatus.BAD_REQUEST, "Choose a PDF to check.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, String>> tooBig(MaxUploadSizeExceededException e) {
        return error(HttpStatus.CONTENT_TOO_LARGE, "That file is over 5 MB. A resume PDF is usually under 1 MB.");
    }

    @ExceptionHandler(UnreadablePdfException.class)
    ResponseEntity<Map<String, String>> unreadable(UnreadablePdfException e) {
        // log the reason only, never the file
        log.info("Unreadable PDF: {}", e.getCause() == null ? e.getMessage() : e.getCause().toString());
        return error(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
