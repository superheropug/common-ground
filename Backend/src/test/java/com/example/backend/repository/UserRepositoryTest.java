package com.example.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import com.example.backend.model.User;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.default_schema=",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndVerifyPassword() {
        User u = new User();
        u.setUsername("alice");
        u.setPassword("s3cr3tPass");

        User saved = userRepository.save(u);
        assertThat(saved.getId()).isNotNull();

        User fetched = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(fetched.verifyPassword("s3cr3tPass")).isTrue();
        assertThat(fetched.verifyPassword("wrong")).isFalse();
    }
}
