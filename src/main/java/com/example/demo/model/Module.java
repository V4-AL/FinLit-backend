package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JSON module id value, such as "module-1"
    private String moduleId;

    // The module order within the course
    @Column(name = "module_order")
    private int moduleOrder;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
}
