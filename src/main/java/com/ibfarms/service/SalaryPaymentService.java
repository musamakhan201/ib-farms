package com.ibfarms.service;

import com.ibfarms.dto.SalaryPaymentDto;
import com.ibfarms.entity.Employee;
import com.ibfarms.entity.SalaryPayment;
import com.ibfarms.exception.DuplicateResourceException;
import com.ibfarms.repository.SalaryPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalaryPaymentService {

    private final SalaryPaymentRepository salaryPaymentRepository;
    private final EmployeeService employeeService;

    @Transactional
    public SalaryPayment record(Long employeeId, SalaryPaymentDto dto) {
        Employee employee = employeeService.getOwned(employeeId);
        if (salaryPaymentRepository.findByEmployeeAndSalaryYearAndSalaryMonth(
                employee, dto.getSalaryYear(), dto.getSalaryMonth()).isPresent()) {
            throw new DuplicateResourceException(
                    "Salary for " + dto.getSalaryMonth() + "/" + dto.getSalaryYear() + " already recorded");
        }
        SalaryPayment payment = SalaryPayment.builder()
                .employee(employee)
                .salaryYear(dto.getSalaryYear())
                .salaryMonth(dto.getSalaryMonth())
                .amount(dto.getAmount())
                .paidDate(dto.getPaidDate())
                .notes(dto.getNotes())
                .build();
        return salaryPaymentRepository.save(payment);
    }

    @Transactional
    public void delete(Long employeeId, Long paymentId) {
        Employee employee = employeeService.getOwned(employeeId);
        SalaryPayment payment = salaryPaymentRepository.findById(paymentId)
                .filter(p -> p.getEmployee().getId().equals(employee.getId()))
                .orElseThrow(() -> new com.ibfarms.exception.ResourceNotFoundException("Salary payment not found"));
        salaryPaymentRepository.delete(payment);
    }
}
