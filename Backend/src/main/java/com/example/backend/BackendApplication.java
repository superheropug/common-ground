package com.example.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.backend.Services.JWTService;

@SpringBootApplication
public class BackendApplication {
    private static String home = System.getProperty("user.home");
    public static void main(String[] args) throws Exception {
        JWTService.setSecretKey(getSecret());
        SpringApplication.run(BackendApplication.class, args);
    }
    private static SecretKey getSecret() throws Exception{
        try{
            if (!Files.exists(Path.of(home, ".secret"))) createSecretFile();
            String str = Files.readString(Path.of(home, ".secret")).trim();
            byte[] bytes = Base64.getDecoder().decode(str);
            return new SecretKeySpec(bytes, "HmacSHA256");
        } catch (Exception e){
            throw new RuntimeException("Secret key not found, check secret path.", e);
        }
        
        // Generate a key at that location with
        // openssl rand -base64 32 > ~/.minesblaster-secret
    }
    private static void createSecretFile() throws NoSuchAlgorithmException, IOException{
        var keygen = KeyGenerator.getInstance("HmacSHA256");
        keygen.init(256);
        String encoded = Base64.getEncoder().encodeToString(keygen.generateKey().getEncoded());
        Files.writeString(Path.of(home, ".secret"), encoded);
    }
}


