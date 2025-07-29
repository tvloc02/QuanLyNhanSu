package com.hrms.controller.web;

import com.hrms.entity.Employee;
import com.hrms.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public String showReportsPage(Model model) {
        try {
            // Lấy dữ liệu tổng quan
            List<Employee> allEmployees = employeeService.getAllEmployees();
            
            // Tính toán thống kê tổng
            BigDecimal totalSalary = allEmployees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avgSalary = allEmployees.isEmpty() ? BigDecimal.ZERO :
                totalSalary.divide(BigDecimal.valueOf(allEmployees.size()), 2, RoundingMode.HALF_UP);
            
            // Thống kê theo tháng hiện tại
            YearMonth currentMonth = YearMonth.now();
            long newEmployeesThisMonth = allEmployees.stream()
                .filter(emp -> {
                    YearMonth hireMonth = YearMonth.from(emp.getHireDate());
                    return hireMonth.equals(currentMonth);
                })
                .count();
            
            BigDecimal totalSalaryThisMonth = allEmployees.stream()
                .filter(emp -> {
                    YearMonth hireMonth = YearMonth.from(emp.getHireDate());
                    return !hireMonth.isAfter(currentMonth);
                })
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Thống kê theo năm hiện tại
            int currentYear = LocalDate.now().getYear();
            long newEmployeesThisYear = allEmployees.stream()
                .filter(emp -> emp.getHireDate().getYear() == currentYear)
                .count();
            
            BigDecimal totalSalaryThisYear = allEmployees.stream()
                .filter(emp -> emp.getHireDate().getYear() <= currentYear)
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Thống kê theo trạng thái
            Map<String, Long> employeesByStatus = allEmployees.stream()
                .collect(Collectors.groupingBy(
                    emp -> emp.getStatus().toString(),
                    Collectors.counting()
                ));
            
            // Top 10 nhân viên có lương cao nhất
            List<Employee> topPaidEmployees = allEmployees.stream()
                .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                .limit(10)
                .collect(Collectors.toList());
            
            // Thống kê nhân viên mới theo tháng trong năm hiện tại
            Map<Integer, Long> newEmployeesByMonth = allEmployees.stream()
                .filter(emp -> emp.getHireDate().getYear() == currentYear)
                .collect(Collectors.groupingBy(
                    emp -> emp.getHireDate().getMonthValue(),
                    Collectors.counting()
                ));

            model.addAttribute("totalEmployees", allEmployees.size());
            model.addAttribute("totalSalary", totalSalary);
            model.addAttribute("avgSalary", avgSalary);
            model.addAttribute("newEmployeesThisMonth", newEmployeesThisMonth);
            model.addAttribute("totalSalaryThisMonth", totalSalaryThisMonth);
            model.addAttribute("newEmployeesThisYear", newEmployeesThisYear);
            model.addAttribute("totalSalaryThisYear", totalSalaryThisYear);
            model.addAttribute("employeesByStatus", employeesByStatus);
            model.addAttribute("topPaidEmployees", topPaidEmployees);
            model.addAttribute("newEmployeesByMonth", newEmployeesByMonth);
            model.addAttribute("currentYear", currentYear);
            model.addAttribute("currentMonth", currentMonth.getMonthValue());

            return "reports";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu báo cáo: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/filter")
    public String filterReports(@RequestParam("reportType") String reportType,
                               @RequestParam(value = "year", required = false) Integer year,
                               @RequestParam(value = "month", required = false) Integer month,
                               Model model) {
        try {
            List<Employee> allEmployees = employeeService.getAllEmployees();
            
            // Tính toán thống kê tổng (luôn hiển thị)
            BigDecimal totalSalary = allEmployees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avgSalary = allEmployees.isEmpty() ? BigDecimal.ZERO :
                totalSalary.divide(BigDecimal.valueOf(allEmployees.size()), 2, RoundingMode.HALF_UP);
            
            // Thống kê theo trạng thái (luôn hiển thị)
            Map<String, Long> employeesByStatus = allEmployees.stream()
                .collect(Collectors.groupingBy(
                    emp -> emp.getStatus().toString(),
                    Collectors.counting()
                ));
            
            // Top 10 nhân viên có lương cao nhất (luôn hiển thị)
            List<Employee> topPaidEmployees = allEmployees.stream()
                .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                .limit(10)
                .collect(Collectors.toList());

            // Thống kê theo loại báo cáo được chọn
            long newEmployeesCount = 0;
            BigDecimal totalSalaryPeriod = BigDecimal.ZERO;
            List<Employee> filteredEmployees = allEmployees;
            
            if ("monthly".equals(reportType) && month != null && year != null) {
                YearMonth targetMonth = YearMonth.of(year, month);
                newEmployeesCount = allEmployees.stream()
                    .filter(emp -> {
                        YearMonth hireMonth = YearMonth.from(emp.getHireDate());
                        return hireMonth.equals(targetMonth);
                    })
                    .count();
                
                totalSalaryPeriod = allEmployees.stream()
                    .filter(emp -> {
                        YearMonth hireMonth = YearMonth.from(emp.getHireDate());
                        return !hireMonth.isAfter(targetMonth);
                    })
                    .map(Employee::getSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                filteredEmployees = allEmployees.stream()
                    .filter(emp -> {
                        YearMonth hireMonth = YearMonth.from(emp.getHireDate());
                        return hireMonth.equals(targetMonth);
                    })
                    .collect(Collectors.toList());
                
            } else if ("yearly".equals(reportType) && year != null) {
                newEmployeesCount = allEmployees.stream()
                    .filter(emp -> emp.getHireDate().getYear() == year)
                    .count();
                
                totalSalaryPeriod = allEmployees.stream()
                    .filter(emp -> emp.getHireDate().getYear() <= year)
                    .map(Employee::getSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                filteredEmployees = allEmployees.stream()
                    .filter(emp -> emp.getHireDate().getYear() == year)
                    .collect(Collectors.toList());
            }
            
            // Thống kê nhân viên mới theo tháng trong năm được chọn
            Map<Integer, Long> newEmployeesByMonth = allEmployees.stream()
                .filter(emp -> emp.getHireDate().getYear() == (year != null ? year : LocalDate.now().getYear()))
                .collect(Collectors.groupingBy(
                    emp -> emp.getHireDate().getMonthValue(),
                    Collectors.counting()
                ));

            model.addAttribute("totalEmployees", allEmployees.size());
            model.addAttribute("totalSalary", totalSalary);
            model.addAttribute("avgSalary", avgSalary);
            model.addAttribute("employeesByStatus", employeesByStatus);
            model.addAttribute("topPaidEmployees", topPaidEmployees);
            model.addAttribute("newEmployeesByMonth", newEmployeesByMonth);
            model.addAttribute("currentYear", LocalDate.now().getYear());
            model.addAttribute("currentMonth", LocalDate.now().getMonthValue());
            
            // Dữ liệu theo filter
            model.addAttribute("reportType", reportType);
            model.addAttribute("selectedYear", year);
            model.addAttribute("selectedMonth", month);
            model.addAttribute("newEmployeesCount", newEmployeesCount);
            model.addAttribute("totalSalaryPeriod", totalSalaryPeriod);
            model.addAttribute("filteredEmployees", filteredEmployees);

            return "reports";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu báo cáo: " + e.getMessage());
            return "reports";
        }
    }
} 