package com.ledger.account.repository;

import com.ledger.account.model.LedgerTx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public interface TxRepository extends JpaRepository<LedgerTx, String> {
    
    // Dynamic runtime aggregation ensures correct math regardless of event arrival sequence
    @Query("SELECT SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE -t.amount END) " +
           "FROM LedgerTx t WHERE t.accountId = :accountId")
    BigDecimal calculateBalance(@Param("accountId") String accountId);
}
