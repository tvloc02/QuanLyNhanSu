package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String location;

    // Một phòng ban có một quản lý (manager)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties({"department", "employees"})
    private Employee manager;

    // Một phòng ban có nhiều nhân viên
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"department"})
    private List<Employee> employees;

    // ==========================
    // Constructors
    // ==========================

    // Constructor mặc định (bắt buộc cho JPA)
    public Department() {
    }

    // Constructor đầy đủ tham số
    public Department(String name, String description, String location, Employee manager, List<Employee> employees) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.manager = manager;
        this.employees = employees;
    }

    // Constructor cơ bản
    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ==========================
    // Getters và Setters
    // ==========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    // ==========================
    // toString (không in ra các entity liên quan để tránh vòng lặp vô hạn)
    // ==========================
    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
