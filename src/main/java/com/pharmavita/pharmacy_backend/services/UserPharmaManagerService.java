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

/**
 * Service pour gérer les utilisateurs pharmaciens (création, mise à jour, suppression, liste).
 * Fournit des fonctionnalités pour administrer les comptes pharmaciens avec audit et envoi d'emails.
 */
@Service
@RequiredArgsConstructor
public class UserPharmaManagerService {
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String ENTITY_TYPE = "USER";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";
    private static final String ACTION_DELETE = "DELETE";
    private static final String ACTION_EMAIL = "EMAIL_SENT";

    /**
     * Valide les données d'un pharmacien avant création ou mise à jour.
     * Vérifie que l'email n'est pas déjà utilisé par un autre utilisateur.
     *
     * @param request Données du pharmacien (email, prénom, nom).
     * @param userId  ID de l'utilisateur (null pour création, non null pour mise à jour).
     * @throws IllegalArgumentException si l'email est déjà utilisé.
     */
    private void validatePharmacist(UserRequest request, Long userId) {
        userRepository.findByEmail(request.email())
            .ifPresent(user -> {
                if (userId == null || !user.getId().equals(userId)) {
                    throw new IllegalArgumentException("Cet email est déjà utilisé");
                }
            });
    }

    /**
     * Génère un mot de passe sécurisé aléatoire de 12 caractères.
     *
     * @return Mot de passe encodé en Base64.
     */
    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, 12);
    }

    /**
     * Enregistre un log d'audit pour une action sur un utilisateur.
     *
     * @param id           ID de l'utilisateur (peut être null pour création).
     * @param actionType   Type d'action (CREATE, UPDATE, DELETE, EMAIL_SENT).
     * @param details      Détails de l'action.
     * @param performedBy  Email de l'administrateur effectuant l'action.
     */
    private void auditLogAction(Long id, String actionType, String details, String performedBy) {
        AuditLog log = new AuditLog();
        log.setEntityId(id);
        log.setEntityType(ENTITY_TYPE);
        log.setAction(actionType);
        log.setDetails(details);
        log.setPerformedBy(performedBy);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    /**
     * Crée un nouveau pharmacien avec un mot de passe généré et envoie un email de bienvenue.
     *
     * @param request     Données du pharmacien (email, prénom, nom).
     * @param adminEmail  Email de l'administrateur effectuant la création.
     * @return            Utilisateur créé.
     * @throws MessagingException si l'envoi de l'email échoue.
     * @throws IllegalArgumentException si l'email est déjà utilisé.
     */
    public User createPharmacist(UserRequest request, String adminEmail) throws MessagingException {
        validatePharmacist(request, null);
        String password = generatePassword();
        User user = new User();
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.PHARMACIST);
        user = userRepository.save(user);

        auditLogAction(user.getId(), ACTION_CREATE, "Création du pharmacien: " + request.email(), adminEmail);
        emailService.sendWelcomeEmail(request.email(), request.firstname(), request.email(), password);
        auditLogAction(user.getId(), ACTION_EMAIL, "Envoi email de bienvenue à: " + request.email(), adminEmail);

        return user;
    }

    /**
     * Liste tous les pharmaciens (utilisateurs avec rôle PHARMACIST).
     *
     * @return Liste des pharmaciens.
     */
    public List<User> listPharmacist() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRole() == Role.PHARMACIST)
            .toList();
    }

    /**
     * Récupère un pharmacien par son ID.
     *
     * @param id ID de l'utilisateur.
     * @return   Utilisateur trouvé.
     * @throws IllegalArgumentException si l'utilisateur n'existe pas.
     */
    public User getPharmacistById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    /**
     * Met à jour les informations d'un pharmacien et envoie un email avec un nouveau mot de passe.
     *
     * @param id          ID de l'utilisateur à mettre à jour.
     * @param request     Nouvelles données du pharmacien.
     * @param adminEmail  Email de l'administrateur effectuant la mise à jour.
     * @return            Utilisateur mis à jour.
     * @throws MessagingException si l'envoi de l'email échoue.
     * @throws IllegalArgumentException si l'utilisateur n'existe pas ou si l'email est déjà utilisé.
     */
    public User updatePharmacist(Long id, UserRequest request, String adminEmail) throws MessagingException {
        User user = getPharmacistById(id);
        validatePharmacist(request, id);

        String oldInfo = "Email: " + user.getEmail() + ", Prénom: " + user.getFirstname() + ", Nom: " + user.getLastname();

        // Générer un nouveau mot de passe dans tous les cas
        String newPassword = generatePassword();
        user.setPassword(passwordEncoder.encode(newPassword));

        // Mettre à jour les champs
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setRole(Role.PHARMACIST);
        user = userRepository.save(user);

        String newInfo = "Email: " + user.getEmail() + ", Prénom: " + user.getFirstname() + ", Nom: " + user.getLastname();
        auditLogAction(id, ACTION_UPDATE, "Avant: " + oldInfo + "; Après: " + newInfo, adminEmail);

        // Envoyer l'email avec les nouveaux identifiants
        emailService.sendUpdatedCredentialsEmail(request.email(), request.firstname(), request.email(), newPassword);
        auditLogAction(id, ACTION_EMAIL, "Envoi nouveaux identifiants à: " + request.email(), adminEmail);

        return user;
    }

    /**
     * Supprime un pharmacien par son ID.
     *
     * @param id         ID de l'utilisateur à supprimer.
     * @param adminEmail Email de l'administrateur effectuant la suppression.
     * @throws IllegalArgumentException si l'utilisateur n'existe pas.
     */
    public void deletePharmacist(Long id, String adminEmail) {
        User user = getPharmacistById(id);
        String email = user.getEmail();
        userRepository.delete(user);
        auditLogAction(id, ACTION_DELETE, "Suppression du pharmacien: " + email, adminEmail);
    }
}