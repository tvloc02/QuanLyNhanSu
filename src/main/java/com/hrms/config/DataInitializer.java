 package com.hrms.config;

import com.hrms.entity.Department;
import com.hrms.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Khởi tạo dữ liệu mẫu khi ứng dụng khởi động
@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentService departmentService;

    @Autowired
    public DataInitializer(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Khởi tạo phòng ban chỉ khi chưa tồn tại
        Department itDept;
        if (!departmentService.existsByName("IT")) {
            itDept = new Department("IT", "Phòng công nghệ thông tin", "Hà Nội", null, null);
            itDept = departmentService.saveDepartment(itDept);
        } else {
            itDept = departmentService.getDepartmentByName("IT").orElseThrow();
        }

        Department hrDept;
        if (!departmentService.existsByName("HR")) {
            hrDept = new Department("HR", "Phòng nhân sự", "TP.HCM", null, null);
            hrDept = departmentService.saveDepartment(hrDept);
        } else {
            hrDept = departmentService.getDepartmentByName("HR").orElseThrow();
        }

        // Thêm 2 phòng ban mới
        Department financeDept;
        if (!departmentService.existsByName("Finance")) {
            financeDept = new Department("Finance", "Phòng tài chính", "Đà Nẵng", null, null);
            financeDept = departmentService.saveDepartment(financeDept);
        } else {
            financeDept = departmentService.getDepartmentByName("Finance").orElseThrow();
        }

        Department marketingDept;
        if (!departmentService.existsByName("Marketing")) {
            marketingDept = new Department("Marketing", "Phòng marketing", "Hải Phòng", null, null);
            marketingDept = departmentService.saveDepartment(marketingDept);
        } else {
            marketingDept = departmentService.getDepartmentByName("Marketing").orElseThrow();
        }
    }
}
