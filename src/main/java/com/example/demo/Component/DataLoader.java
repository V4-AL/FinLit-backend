package com.example.demo.Component;

import org.springframework.stereotype.Component;
import com.example.demo.model.Lesson;
import com.example.demo.model.Module;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.ModuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader {

    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;

    public DataLoader(LessonRepository lessonRepository,ModuleRepository moduleRepository) {
        this.lessonRepository = lessonRepository;
        this.moduleRepository = moduleRepository;
    }
    
    @PostConstruct
    public void loadData() throws Exception {
        // Check if lessons already exist
        if (lessonRepository.count() > 0) {
            return; // Skip if data already loaded
        }
        
        // Read JSON file
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getResourceAsStream("/financial_literacy_course.json");
        if (inputStream == null) {
            throw new IllegalStateException("Resource not found: /financial_literacy_course.json");
        }
        JsonNode root = mapper.readTree(inputStream);
        
        List<Lesson> lessons = new ArrayList<>();
        List<Module> modulesToSave = new ArrayList<>();
        
        // Extract lessons from modules
        JsonNode modules = root.path("course").path("modules");
        modules.forEach(module -> {
            Module moduleEntity = new Module();
            moduleEntity.setModuleId(module.path("id").asText());
            moduleEntity.setModuleOrder(module.path("order").asInt());
            moduleEntity.setTitle(module.path("title").asText());
            moduleEntity.setDescription(module.path("description").asText(""));
            modulesToSave.add(moduleEntity);

            JsonNode lessonsList = module.path("lessons");
            lessonsList.forEach(lessonNode -> {
                Lesson lesson = new Lesson();
                lesson.setTitle(lessonNode.path("title").asText());
                lesson.setDescription(lessonNode.path("id").asText()); // Use id as description for now
                lesson.setContent(lessonNode.path("content_blocks").toString()); // Store as JSON string
                lesson.setDuration(30); // Default duration
                lessons.add(lesson);
            });
        });
        
        // Save modules first, then lessons
        moduleRepository.saveAll(modulesToSave);
        lessonRepository.saveAll(lessons);
        System.out.println("Loaded " + modulesToSave.size() + " modules and " + lessons.size() + " lessons from JSON");
    }
}
