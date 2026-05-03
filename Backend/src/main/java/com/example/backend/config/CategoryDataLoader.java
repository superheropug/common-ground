package com.example.backend.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.model.Category;
import com.example.backend.repository.CategoryRepository;

@Component
public class CategoryDataLoader implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    public CategoryDataLoader(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        List<Category> desired = List.of(
                new Category("AUDIO_SENSITIVITY", "Audio Sensitivity"),
                new Category("LIM_MOBILITY", "Limited Mobility"),
                new Category("WHEELCHAIR", "Wheelchair"),
                new Category("LIMITED_VISON", "Limited/Impaired Vision"),
                new Category("BLIND", "Blind")
                // More categories can be made as needed.
        );

        for (Category c : desired) {
            if (!categoryRepository.existsById(c.getName())) {
                categoryRepository.save(c);
            }
        }
    }
}
