package com.hrms.controller.web;

import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.service.DepartmentService;
import com.hrms.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            // Lấy dữ liệu tổng quan
            List<Employee> employees = employeeService.getAllEmployees();
            List<Department> departments = departmentService.getAllDepartments();

            // Tính toán thống kê
            int totalEmployees = employees.size();
            int totalDepartments = departments.size();
            
            BigDecimal totalSalary = employees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avgSalary = employees.isEmpty() ? BigDecimal.ZERO :
                totalSalary.divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);

            // Thống kê theo phòng ban
            Map<String, Long> employeesByDepartment = employees.stream()
                .filter(emp -> emp.getDepartment() != null)
                .collect(Collectors.groupingBy(
                    emp -> emp.getDepartment().getName(),
                    Collectors.counting()
                ));

            // Thống kê nhân viên mới trong tháng này
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            long newEmployeesThisMonth = employees.stream()
                .filter(emp -> emp.getHireDate().isAfter(firstDayOfMonth.minusDays(1)))
                .count();

            // Top 5 nhân viên có lương cao nhất
            List<Employee> topPaidEmployees = employees.stream()
                .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                .limit(5)
                .collect(Collectors.toList());

            // Thống kê theo trạng thái
            Map<String, Long> employeesByStatus = employees.stream()
                .collect(Collectors.groupingBy(
                    emp -> emp.getStatus().toString(),
                    Collectors.counting()
                ));

            model.addAttribute("totalEmployees", totalEmployees);
            model.addAttribute("totalDepartments", totalDepartments);
            model.addAttribute("totalSalary", totalSalary);
            model.addAttribute("avgSalary", avgSalary);
            model.addAttribute("newEmployeesThisMonth", newEmployeesThisMonth);
            model.addAttribute("employeesByDepartment", employeesByDepartment);
            model.addAttribute("topPaidEmployees", topPaidEmployees);
            model.addAttribute("employeesByStatus", employeesByStatus);
            model.addAttribute("recentEmployees", employees.stream()
                .sorted((e1, e2) -> e2.getHireDate().compareTo(e1.getHireDate()))
                .limit(5)
                .collect(Collectors.toList()));

            return "dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu trang chủ: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/login?logout";
    }
}