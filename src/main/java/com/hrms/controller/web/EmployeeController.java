package com.hrms.controller.web;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hrms.entity.Employee;
import com.hrms.entity.EmployeeStatus;
import com.hrms.service.DepartmentService;
import com.hrms.service.EmployeeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    // Constructor Injection (DI) - Inject EmployeeService vào EmployeeController
    @Autowired
    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    // Hiển thị bảng lương
    @GetMapping("/salary-table")
    public String showSalaryTable(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "salary-table";
    }

    @GetMapping
    public String listEmployees(Model model, @RequestParam(value = "success", required = false) Boolean success) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        if (success != null && success) {
            model.addAttribute("successMessage", "Nhân viên đã được thêm thành công!");
        }
        return "employee-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
        return "employee-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
        return "employee-form";
    }

    @GetMapping("/{id}")
    public String viewEmployeeDetail(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        model.addAttribute("employee", employee);
        return "employee-detail";
    }

    @PostMapping("/save")
    public String saveEmployee(@ModelAttribute("employee") @Valid Employee employee, 
                              @RequestParam(value = "department.id", required = false) Long departmentId,
                              BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
            return "employee-form";
        }
        
        try {
            // Debug: In ra thông tin employee trước khi lưu
            System.out.println("Employee before save: " + employee);
            System.out.println("Phone number: " + employee.getPhoneNumber());
            
            // Xử lý department
            if (departmentId != null) {
                var department = departmentService.getDepartmentById(departmentId);
                if (department.isPresent()) {
                    employee.setDepartment(department.get());
                } else {
                    bindingResult.rejectValue("department", "error.department", "Phòng ban không tồn tại");
                    model.addAttribute("departments", departmentService.getAllDepartments());
                    model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
                    return "employee-form";
                }
            }
            
            // Đảm bảo có ngày vào làm
            if (employee.getHireDate() == null) {
                employee.setHireDate(LocalDate.now());
            }
            
            // Đảm bảo có trạng thái
            if (employee.getStatus() == null) {
                employee.setStatus(EmployeeStatus.ACTIVE);
            }
            
            // Đảm bảo có số điện thoại
            if (employee.getPhoneNumber() == null || employee.getPhoneNumber().trim().isEmpty()) {
                bindingResult.rejectValue("phoneNumber", "error.phoneNumber", "Số điện thoại không được để trống");
                model.addAttribute("departments", departmentService.getAllDepartments());
                model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
                return "employee-form";
            }
            
            // Kiểm tra email trùng lặp
            if (employee.getEmail() != null && !employee.getEmail().trim().isEmpty()) {
                var existingEmployee = employeeService.getEmployeeByEmail(employee.getEmail().trim());
                if (existingEmployee.isPresent()) {
                    bindingResult.rejectValue("email", "error.email", "Email này đã được sử dụng bởi nhân viên khác");
                    model.addAttribute("departments", departmentService.getAllDepartments());
                    model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
                    return "employee-form";
                }
            }
            
            employeeService.saveEmployee(employee);
            return "redirect:/employees?success=true";
        } catch (Exception e) {
            // Log lỗi
            System.err.println("Error saving employee: " + e.getMessage());
            e.printStackTrace();
            
            // Thêm lỗi vào model
            model.addAttribute("error", "Có lỗi xảy ra khi lưu nhân viên: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
            return "employee-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
}