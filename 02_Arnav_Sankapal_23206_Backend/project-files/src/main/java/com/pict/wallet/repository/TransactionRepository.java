package com.pict.wallet.repository;

import com.pict.wallet.model.WalletTransaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<WalletTransaction, Long> {
    // Recipients see successful incoming transfers; failed attempts belong to the initiator.
    @Query("""
        select t from WalletTransaction t
        where t.initiator.id = :userId
           or (t.recipient.id = :userId and t.status = com.pict.wallet.model.TransactionStatus.SUCCESS)
        order by t.createdAt desc, t.id desc
        """)
    List<WalletTransaction> findHistory(@Param("userId") Long userId);
}
