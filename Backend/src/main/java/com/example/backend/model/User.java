package com.example.backend.model;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "USERS")
public class User {

    @Transient
    private static final SecureRandom random = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "hashed_password", nullable = false)
    private byte[] hashedPassword;

    @Column(name = "salt", nullable = false)
    private byte[] salt;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public byte[] getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(byte[] hashedPassword) { this.hashedPassword = hashedPassword; }

    public byte[] getSalt() { return salt; }
    public void setSalt(byte[] salt) { this.salt = salt; }

    // Generate a new salt and hash the provided plain-text password, storing both
    public void setPassword(String plainPassword) {
        byte[] newSalt = generateSalt();
        this.salt = newSalt;
        this.hashedPassword = hashPassword(plainPassword, newSalt);
    }

    // Verify a plain-text password against stored hash
    public boolean verifyPassword(String plainPassword) {
        if (this.salt == null || this.hashedPassword == null) return false;
        byte[] candidate = hashPassword(plainPassword, this.salt);
        if (candidate.length != this.hashedPassword.length) return false;
        int diff = 0;
        for (int i = 0; i < candidate.length; i++) {
            diff |= candidate[i] ^ this.hashedPassword[i];
        }
        return diff == 0;
    }

    private static byte[] generateSalt() {
        byte[] s = new byte[16];
        random.nextBytes(s);
        return s;
    }

    private static byte[] hashPassword(String password, byte[] salt) {
        try {
            int iterations = 100_000;
            int keyLength = 256;
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
