package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Họ không được để trống")
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Column(name = "last_name")
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(name = "phone")
    private String phoneNumber;

    @NotNull(message = "Lương cơ bản không được để trống")
    @DecimalMin(value = "0", message = "Lương phải lớn hơn 0")
    @Column(name = "salary")
    private BigDecimal salary;

    @NotNull(message = "Ngày vào làm không được để trống")
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "position")
    private String position; // Thêm trường chức vụ

    @NotNull(message = "Phòng ban không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"employees", "manager"})
    private Department department;

    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmployeeStatus status;

    // Constructors
    public Employee() {}

    public Employee(String firstName, String lastName, String email, String phone, BigDecimal salary, Department department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phone;
        this.salary = salary;
        this.department = department;
        this.hireDate = LocalDate.now();
        this.status = EmployeeStatus.ACTIVE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public EmployeeStatus getStatus() { return status; }
    public void setStatus(EmployeeStatus status) { this.status = status; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
