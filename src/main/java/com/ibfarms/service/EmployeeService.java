package com.ibfarms.service;

import com.ibfarms.dto.EmployeeFormDto;
import com.ibfarms.entity.Employee;
import com.ibfarms.entity.User;
import com.ibfarms.exception.ResourceNotFoundException;
import com.ibfarms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findByOwnerOrderByFullNameAsc(currentUserService.getCurrentUser());
    }

    @Transactional(readOnly = true)
    public Employee getOwned(Long id) {
        User owner = currentUserService.getCurrentUser();
        return employeeRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Transactional(readOnly = true)
    public Employee getOwnedWithSalaries(Long id) {
        User owner = currentUserService.getCurrentUser();
        return employeeRepository.findByIdAndOwnerWithSalaries(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Transactional
    public Employee create(EmployeeFormDto dto) {
        User owner = currentUserService.getCurrentUser();
        Employee employee = mapToEntity(new Employee(), dto, owner);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, EmployeeFormDto dto) {
        Employee employee = getOwned(id);
        mapToEntity(employee, dto, employee.getOwner());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void delete(Long id) {
        employeeRepository.delete(getOwned(id));
    }

    public EmployeeFormDto toFormDto(Employee employee) {
        EmployeeFormDto dto = new EmployeeFormDto();
        dto.setId(employee.getId());
        dto.setFullName(employee.getFullName());
        dto.setRole(employee.getRole());
        dto.setPhone(employee.getPhone());
        dto.setMonthlySalary(employee.getMonthlySalary());
        dto.setHireDate(employee.getHireDate());
        dto.setActive(employee.isActive());
        return dto;
    }

    private Employee mapToEntity(Employee employee, EmployeeFormDto dto, User owner) {
        employee.setFullName(dto.getFullName().trim());
        employee.setRole(dto.getRole() != null ? dto.getRole().trim() : null);
        employee.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        employee.setMonthlySalary(dto.getMonthlySalary());
        employee.setHireDate(dto.getHireDate());
        employee.setActive(dto.isActive());
        employee.setOwner(owner);
        return employee;
    }
}
