package com.pharmavita.pharmacy_backend.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.pharmavita.pharmacy_backend.config.utils.JwtUtil;
import com.pharmavita.pharmacy_backend.models.User;
import com.pharmavita.pharmacy_backend.models.records.LoginRequest;
import com.pharmavita.pharmacy_backend.models.records.RegisterRequest;
import com.pharmavita.pharmacy_backend.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    public String login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(userDetails);
    }

    public User register(RegisterRequest request){
        if (userRepository.findByEmail(request.email()).isPresent()){
            throw new IllegalArgumentException("Email already exist");
        }
        User user = new User();
        
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        return userRepository.save(user);

    }

}
