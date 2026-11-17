package com.trainerstack.studentapi.controller;

import com.trainerstack.studentapi.model.Student;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final List<Student> students = List.of(
            new Student(1L, "Arun", "AWS"),
            new Student(2L, "Priya", "DevOps"),
            new Student(3L, "Karthik", "Kubernetes")
    );

    @GetMapping
    public List<Student> getAllStudents() {
        return students;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        return students.stream()
                .filter(student -> student.id().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
