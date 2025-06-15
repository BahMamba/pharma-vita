package com.pharmavita.pharmacy_backend.config.exception;

  import jakarta.mail.MessagingException;
  import jakarta.validation.ConstraintViolationException;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.http.HttpStatus;
  import org.springframework.http.ResponseEntity;
  import org.springframework.security.access.AccessDeniedException;
  import org.springframework.security.core.AuthenticationException;
  import org.springframework.web.bind.annotation.ControllerAdvice;
  import org.springframework.web.bind.annotation.ExceptionHandler;

  import java.util.NoSuchElementException;

  @ControllerAdvice
  public class GlobalExceptionHandler {
      private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

      @ExceptionHandler(AuthenticationException.class)
      public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
          ErrorResponse error = new ErrorResponse("AUTH_FAILED", "Informations incorrect");
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
      }

      @ExceptionHandler(AccessDeniedException.class)
      public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
          ErrorResponse error = new ErrorResponse("UNAUTHORIZED", "Accès non réservé");
          return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
      }

      @ExceptionHandler(IllegalArgumentException.class)
      public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
          ErrorResponse error = new ErrorResponse("INVALID_REQUEST", ex.getMessage());
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
      }

      @ExceptionHandler(ConstraintViolationException.class)
      public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
          String message = ex.getConstraintViolations().stream()
              .map(violation -> violation.getMessage())
              .findFirst()
              .orElse("Erreur de validation");
          ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", message);
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
      }

      @ExceptionHandler(MessagingException.class)
      public ResponseEntity<ErrorResponse> handleMessagingException(MessagingException ex) {
          logger.error("Erreur lors de l'envoi de l'email", ex);
          ErrorResponse error = new ErrorResponse("EMAIL_ERROR", "Erreur lors de l'envoi de l'email");
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
      }

      @ExceptionHandler(NoSuchElementException.class)
      public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
          ErrorResponse error = new ErrorResponse("NOT_FOUND", "Utilisateur non trouvé");
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
      }

      @ExceptionHandler(Exception.class)
      public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
          logger.error("Erreur inattendue", ex);
          ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Une erreur inattendue s'est produite. Veuillez réessayer");
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
      }

    public record ErrorResponse(String code, String message) {}

  }

