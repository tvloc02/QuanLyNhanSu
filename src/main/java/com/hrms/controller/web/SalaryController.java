package com.hrms.controller.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.service.DepartmentService;
import com.hrms.service.EmployeeService;

@Controller
@RequestMapping("/salaries")
public class SalaryController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String showSalaryTable(Model model) {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            
            // Tính toán thống kê
            BigDecimal totalSalary = employees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avgSalary = employees.isEmpty() ? BigDecimal.ZERO :
                totalSalary.divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal maxSalary = employees.stream()
                .map(Employee::getSalary)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
            
            model.addAttribute("employees", employees);
            model.addAttribute("totalSalary", totalSalary);
            model.addAttribute("avgSalary", avgSalary);
            model.addAttribute("maxSalary", maxSalary);
            return "salary-table";
        } catch (Exception e) {
            // Log error and return error page or redirect
            model.addAttribute("error", "Không thể tải dữ liệu bảng lương: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/")
    public String showSalaryTableWithSlash(Model model) {
        return showSalaryTable(model);
    }

    @GetMapping("/average")
    public String showAverageSalaryPage(Model model) {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            model.addAttribute("departments", departments);
            return "average-salary";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu phòng ban: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/average/calculate")
    public String calculateAverageSalary(@RequestParam("departmentId") Long departmentId,
                                       @RequestParam("year") int year,
                                       @RequestParam("month") int month,
                                       Model model) {
        try {
            Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
            if (departmentOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy phòng ban");
                model.addAttribute("departments", departmentService.getAllDepartments());
                return "average-salary";
            }
            Department department = departmentOpt.get();

            List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
            
            // Lọc nhân viên theo tháng/năm vào làm
            YearMonth targetYearMonth = YearMonth.of(year, month);
            List<Employee> employeesInPeriod = employees.stream()
                .filter(emp -> {
                    YearMonth hireYearMonth = YearMonth.from(emp.getHireDate());
                    return !hireYearMonth.isAfter(targetYearMonth);
                })
                .collect(Collectors.toList());

            if (employeesInPeriod.isEmpty()) {
                model.addAttribute("error", "Không có nhân viên nào trong phòng ban " + department.getName() + " vào tháng " + month + "/" + year);
                model.addAttribute("departments", departmentService.getAllDepartments());
                return "average-salary";
            }

            // Tính lương trung bình
            BigDecimal totalSalary = employeesInPeriod.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal averageSalary = totalSalary.divide(BigDecimal.valueOf(employeesInPeriod.size()), 2, RoundingMode.HALF_UP);

            // Tính lương trung bình theo năm
            Map<Integer, BigDecimal> yearlyAverages = employees.stream()
                .collect(Collectors.groupingBy(
                    emp -> emp.getHireDate().getYear(),
                    Collectors.collectingAndThen(
                        Collectors.mapping(Employee::getSalary, Collectors.toList()),
                        salaries -> {
                            if (salaries.isEmpty()) return BigDecimal.ZERO;
                            return salaries.stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(salaries.size()), 2, RoundingMode.HALF_UP);
                        }
                    )
                ));

            model.addAttribute("department", department);
            model.addAttribute("selectedYear", year);
            model.addAttribute("selectedMonth", month);
            model.addAttribute("employeesInPeriod", employeesInPeriod);
            model.addAttribute("totalSalary", totalSalary);
            model.addAttribute("averageSalary", averageSalary);
            model.addAttribute("employeeCount", employeesInPeriod.size());
            model.addAttribute("yearlyAverages", yearlyAverages);
            model.addAttribute("departments", departmentService.getAllDepartments());

            return "average-salary";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tính toán lương trung bình: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "average-salary";
        }
    }

    @GetMapping("/highest")
    public String showHighestSalaryPage(Model model) {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            model.addAttribute("departments", departments);
            return "highest-salary";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải dữ liệu phòng ban: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/highest/calculate")
    public String calculateHighestSalary(@RequestParam("departmentId") Long departmentId,
                                       Model model) {
        try {
            Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
            if (departmentOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy phòng ban");
                model.addAttribute("departments", departmentService.getAllDepartments());
                return "highest-salary";
            }
            Department department = departmentOpt.get();

            List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
            
            if (employees.isEmpty()) {
                model.addAttribute("error", "Không có nhân viên nào trong phòng ban " + department.getName());
                model.addAttribute("departments", departmentService.getAllDepartments());
                return "highest-salary";
            }

            // Tìm nhân viên có lương cao nhất
            Employee highestPaidEmployee = employees.stream()
                .max((e1, e2) -> e1.getSalary().compareTo(e2.getSalary()))
                .orElse(null);

            // Tính toán thống kê
            BigDecimal totalSalary = employees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avgSalary = totalSalary.divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal minSalary = employees.stream()
                .map(Employee::getSalary)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

            // Sắp xếp nhân viên theo lương giảm dần
            List<Employee> sortedEmployees = employees.stream()
                .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                .collect(Collectors.toList());

            model.addAttribute("department", department);
            model.addAttribute("highestPaidEmployee", highestPaidEmployee);
            model.addAttribute("employees", sortedEmployees);
            model.addAttribute("totalSalary", totalSalary);
            model.addAttribute("avgSalary", avgSalary);
            model.addAttribute("minSalary", minSalary);
            model.addAttribute("employeeCount", employees.size());
            model.addAttribute("departments", departmentService.getAllDepartments());

            return "highest-salary";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tính toán lương cao nhất: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "highest-salary";
        }
    }
}