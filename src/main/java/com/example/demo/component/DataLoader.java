package com.example.demo.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository courseModuleRepository;

    public DataLoader(LessonRepository lessonRepository, CourseModuleRepository courseModuleRepository) {
        this.lessonRepository = lessonRepository;
        this.courseModuleRepository = courseModuleRepository;
    }

    @PostConstruct
    public void loadData() throws Exception {
        // Lessons and modules are seeded independently — a DB that already has lessons
        // from a prior run but is missing modules (or vice versa) must still backfill
        // whichever one is empty, rather than skipping the whole load.
        boolean needsLessons = lessonRepository.count() == 0;
        boolean needsModules = courseModuleRepository.count() == 0;
        if (!needsLessons && !needsModules) {
            return;
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

        if (needsModules) {
            courseModuleRepository.saveAll(courseModulesToSave);
            log.info("Loaded {} course modules from JSON", courseModulesToSave.size());
        }
        if (needsLessons) {
            lessonRepository.saveAll(lessons);
            log.info("Loaded {} lessons from JSON", lessons.size());
        }
    }
}
