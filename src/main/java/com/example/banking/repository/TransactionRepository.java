package com.example.banking.repository;

import com.example.banking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :accountId OR t.toAccount = :accountId ORDER BY t.date DESC")
    List<Transaction> findByAccountId(@Param("accountId") long accountId);

    /**
     * Find fee transactions for a specific account within a date range
     * @param accountId The account ID
     * @param type The transaction type (should be "FEE")
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of fee transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :accountId AND t.type = :type AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    List<Transaction> findByAccountIdAndTypeAndDateBetween(
        @Param("accountId") long accountId, 
        @Param("type") String type, 
        @Param("startDate") Date startDate, 
        @Param("endDate") Date endDate);
} 