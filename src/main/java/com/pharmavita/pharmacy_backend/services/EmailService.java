package com.pharmavita.pharmacy_backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String firstname, String email, String password) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Bienvenue chez PharmaVita !");
        helper.setText(
            """
            <h1>Bienvenue, %s !</h1>
            <p>Votre compte PharmaVita a été créé.</p>
            <p><strong>Email :</strong> %s</p>
            <p><strong>Mot de passe :</strong> %s</p>
            <p>Connectez-vous à <a href="http://localhost:4200/login">PharmaVita</a>.</p>
            <p><i>Changez votre mot de passe après votre première connexion.</i></p>
            """.formatted(firstname, email, password), true
        );
        mailSender.send(message);
    }

    public void sendUpdatedCredentialsEmail(String to, String firstname, String email, String password) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Mise à jour de vos identifiants PharmaVita");
        helper.setText(
            """
            <h1>Bonjour, %s !</h1>
            <p>Vos identifiants PharmaVita ont été mis à jour.</p>
            <p><strong>Email :</strong> %s</p>
            <p><strong>Mot de passe :</strong> %s</p>
            <p>Connectez-vous à <a href="http://localhost:4200/login">PharmaVita</a> avec vos nouveaux identifiants.</p>
            <p><i>Changez votre mot de passe après votre première connexion.</i></p>
            """.formatted(firstname, email, password), true
        );
        mailSender.send(message);
    }
}