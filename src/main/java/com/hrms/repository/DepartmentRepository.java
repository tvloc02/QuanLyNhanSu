package com.hrms.repository;

import com.hrms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Find department by name
    Optional<Department> findByName(String name);

    // Find departments by name containing (for search)
    List<Department> findByNameContainingIgnoreCase(String name);

    // Find departments by location
    List<Department> findByLocation(String location);

    // Check if department name exists (for validation)
    boolean existsByName(String name);

    // Check if department name exists excluding current id (for update validation)
    boolean existsByNameAndIdNot(String name, Long id);

    // Find departments ordered by name
    List<Department> findAllByOrderByNameAsc();

    // Find departments with employee count
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();

    // Find departments by manager
    @Query("SELECT d FROM Department d WHERE d.manager.id = :managerId")
    List<Department> findByManagerId(@Param("managerId") Long managerId);

    // Count total departments
    long count();

    // Find departments with no manager
    @Query("SELECT d FROM Department d WHERE d.manager IS NULL")
    List<Department> findDepartmentsWithoutManager();

    // Search departments by multiple criteria
    @Query("SELECT d FROM Department d WHERE " +
            "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:location IS NULL OR LOWER(d.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<Department> searchDepartments(@Param("name") String name,
                                       @Param("location") String location);
}