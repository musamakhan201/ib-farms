package com.ibfarms.service;

import com.ibfarms.dto.OtherExpenseDto;
import com.ibfarms.entity.OtherExpense;
import com.ibfarms.entity.User;
import com.ibfarms.exception.ResourceNotFoundException;
import com.ibfarms.repository.OtherExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OtherExpenseService {

    private final OtherExpenseRepository otherExpenseRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<OtherExpense> findAll() {
        return otherExpenseRepository.findByOwnerOrderByExpenseDateDesc(currentUserService.getCurrentUser());
    }

    @Transactional(readOnly = true)
    public OtherExpense getOwned(Long id) {
        User owner = currentUserService.getCurrentUser();
        return otherExpenseRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }

    @Transactional
    public OtherExpense create(OtherExpenseDto dto) {
        User owner = currentUserService.getCurrentUser();
        OtherExpense expense = OtherExpense.builder()
                .owner(owner)
                .expenseDate(dto.getExpenseDate())
                .category(dto.getCategory().trim())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .build();
        return otherExpenseRepository.save(expense);
    }

    @Transactional
    public void delete(Long id) {
        otherExpenseRepository.delete(getOwned(id));
    }
}
