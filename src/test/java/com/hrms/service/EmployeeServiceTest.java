package com.hrms.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        testDepartment = new Department("IT", "IT Department");
        testDepartment.setId(1L);

        testEmployee = new Employee("John", "Doe", "john@test.com",
                "123456789", new BigDecimal("50000"), testDepartment);
        testEmployee.setId(1L);
    }

    @Test
    void testGetAllEmployees() {
        // Arrange
        List<Employee> expectedEmployees = Arrays.asList(testEmployee);
        when(employeeRepository.findAll()).thenReturn(expectedEmployees);

        // Act
        List<Employee> actualEmployees = employeeService.getAllEmployees();

        // Assert
        assertEquals(expectedEmployees.size(), actualEmployees.size());
        assertEquals(expectedEmployees.get(0), actualEmployees.get(0));
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void testUpdateEmployeeSalary() {
        // Arrange
        BigDecimal newSalary = new BigDecimal("60000");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        Employee updatedEmployee = employeeService.updateEmployeeSalary(1L, newSalary);

        // Assert
        assertEquals(newSalary, updatedEmployee.getSalary());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(testEmployee);
    }
}