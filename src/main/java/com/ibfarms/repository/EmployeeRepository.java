package com.ibfarms.repository;

import com.ibfarms.entity.Employee;
import com.ibfarms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByOwnerOrderByFullNameAsc(User owner);

    Optional<Employee> findByIdAndOwner(Long id, User owner);

    @Query("""
            SELECT DISTINCT e FROM Employee e
            LEFT JOIN FETCH e.salaryPayments
            WHERE e.id = :id AND e.owner = :owner
            """)
    Optional<Employee> findByIdAndOwnerWithSalaries(@Param("id") Long id, @Param("owner") User owner);

    long countByOwnerAndActiveTrue(User owner);
}
