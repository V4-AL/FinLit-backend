package com.example.demo.Component;

import org.springframework.stereotype.Component;
import com.example.demo.model.Lesson;
import com.example.demo.model.CourseModule;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.CourseModuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository courseModuleRepository;

    public DataLoader(LessonRepository lessonRepository, CourseModuleRepository courseModuleRepository) {
        this.lessonRepository = lessonRepository;
        this.courseModuleRepository = courseModuleRepository;
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
        List<CourseModule> courseModulesToSave = new ArrayList<>();
        
        // Extract lessons from modules
        JsonNode modules = root.path("course").path("modules");
        modules.forEach(module -> {
            CourseModule courseModule = new CourseModule();
            courseModule.setModuleId(module.path("id").asText());
            courseModule.setModuleOrder(module.path("order").asInt());
            courseModule.setTitle(module.path("title").asText());
            courseModule.setDescription(module.path("description").asText(""));
            courseModulesToSave.add(courseModule);

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
        courseModuleRepository.saveAll(courseModulesToSave);
        lessonRepository.saveAll(lessons);
        System.out.println("Loaded " + courseModulesToSave.size() + " course modules and " + lessons.size() + " lessons from JSON");
    }
}
