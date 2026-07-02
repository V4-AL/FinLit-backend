package com.example.demo.controller;

import com.example.demo.model.CourseModule;
import com.example.demo.repository.CourseModuleRepository;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class CourseModuleController {

    private final CourseModuleRepository courseModuleRepository;

    public CourseModuleController(CourseModuleRepository courseModuleRepository) {
        this.courseModuleRepository = courseModuleRepository;
    }

    @GetMapping
    public List<CourseModule> getAllCourseModules() {
        return courseModuleRepository.findAll();
    }

    @GetMapping("/{id}")
    public CourseModule getCourseModuleById(@PathVariable @NonNull Long id) {
        return courseModuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course module not found"));
    }
}
