package com.pharmavita.pharmacy_backend.services;

import com.pharmavita.pharmacy_backend.models.AuditLog;
import com.pharmavita.pharmacy_backend.models.Role;
import com.pharmavita.pharmacy_backend.models.User;
import com.pharmavita.pharmacy_backend.models.records.UserRequest;
import com.pharmavita.pharmacy_backend.repositories.AuditLogRepository;
import com.pharmavita.pharmacy_backend.repositories.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPharmaManagerService {
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public User createPharmacist(UserRequest request, String adminEmail) throws MessagingException {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        String password = generatePassword();
        User user = new User();
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.PHARMACIST);
        userRepository.save(user);

        AuditLog log = new AuditLog();
        log.setAction("Création du pharmacien: " + request.email());
        log.setPerformedBy(adminEmail);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        emailService.sendWelcomeEmail(request.email(), request.firstname(), request.email(), password);

        return user;
    }

    public List<User> listPharmacist() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRole() == Role.PHARMACIST)
            .toList();
    }

    public User getPharmacistById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    public User updatePharmacist(Long id, UserRequest request, String adminEmail) throws MessagingException {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        if (!user.getEmail().equals(request.email()) && userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Générer un nouveau mot de passe si l'email change
        String newPassword = null;
        boolean emailChanged = !user.getEmail().equals(request.email());
        if (emailChanged) {
            newPassword = generatePassword();
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // Mettre à jour les champs
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setRole(Role.PHARMACIST);
        userRepository.save(user);

        // Journaliser l'action
        AuditLog log = new AuditLog();
        log.setAction("Mise à jour du pharmacien: " + request.email());
        log.setPerformedBy(adminEmail);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        // Envoyer l'email si l'email a changé
        if (emailChanged && newPassword != null) {
            emailService.sendUpdatedCredentialsEmail(request.email(), request.firstname(), request.email(), newPassword);

            // Journaliser l'envoi de l'email
            AuditLog emailLog = new AuditLog();
            emailLog.setAction("Envoi des nouveaux identifiants à: " + request.email());
            emailLog.setPerformedBy(adminEmail);
            emailLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(emailLog);
        }

        return user;
    }

    public void deletePharmacist(Long id, String adminEmail) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        userRepository.delete(user);

        AuditLog log = new AuditLog();
        log.setAction("Suppression du pharmacien: " + user.getEmail());
        log.setPerformedBy(adminEmail);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, 12);
    }
}