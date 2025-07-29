package com.hrms.controller.web;

import com.hrms.entity.Employee;
import com.hrms.service.DepartmentService;
import com.hrms.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/salary-calculation")
public class SalaryCalculationController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @Autowired
    public SalaryCalculationController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String showSalaryCalculationForm(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "salary-calculation";
    }

    @PostMapping("/calculate")
    public String calculateSalary(@RequestParam("departmentId") Long departmentId,
                                 @RequestParam("email") String email,
                                 Model model) {
        try {
            // Lấy thông tin phòng ban
            var department = departmentService.getDepartmentById(departmentId);
            if (department.isEmpty()) {
                model.addAttribute("error", "Phòng ban không tồn tại");
                model.addAttribute("departments", departmentService.getAllDepartments());
                return "salary-calculation";
            }

            // Tìm nhân viên theo email
            Optional<Employee> employeeOpt = employeeService.getEmployeeByEmail(email);
            if (employeeOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy nhân viên với email: " + email);
                model.addAttribute("departments", departmentService.getAllDepartments());
                return "salary-calculation";
            }

            Employee employee = employeeOpt.get();

            // Kiểm tra xem nhân viên có thuộc phòng ban đã chọn không
            if (employee.getDepartment() == null || !employee.getDepartment().getId().equals(departmentId)) {
                model.addAttribute("error", "Nhân viên không thuộc phòng ban đã chọn");
                model.addAttribute("departments", departmentService.getAllDepartments());
                return "salary-calculation";
            }

            // Tính tổng lương từ lúc vào làm đến hiện tại
            BigDecimal totalSalary = calculateTotalSalary(employee);

            // Tính thời gian làm việc
            Period workPeriod = Period.between(employee.getHireDate(), LocalDate.now());

            // Thêm thông tin vào model
            model.addAttribute("employee", employee);
            model.addAttribute("department", department.get());
            model.addAttribute("totalSalary", totalSalary);
            model.addAttribute("workPeriod", workPeriod);
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("success", true);

            return "salary-calculation";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "salary-calculation";
        }
    }

    private BigDecimal calculateTotalSalary(Employee employee) {
        if (employee.getHireDate() == null || employee.getSalary() == null) {
            return BigDecimal.ZERO;
        }

        LocalDate hireDate = employee.getHireDate();
        LocalDate currentDate = LocalDate.now();
        
        // Tính số tháng làm việc
        Period period = Period.between(hireDate, currentDate);
        int monthsWorked = period.getYears() * 12 + period.getMonths();
        
        // Nếu chưa đủ 1 tháng thì tính 1 tháng
        if (monthsWorked == 0) {
            monthsWorked = 1;
        }

        // Tính tổng lương = lương cơ bản * số tháng làm việc
        return employee.getSalary().multiply(BigDecimal.valueOf(monthsWorked));
    }
} 