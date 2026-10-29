package com.pict.wallet.repository;

import com.pict.wallet.model.WalletUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<WalletUser, Long> {
    Optional<WalletUser> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("select u.id from WalletUser u where u.email = :email")
    Optional<Long> findIdByEmail(@Param("email") String email);

    // A database row lock makes another balance update wait for this transaction.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from WalletUser u where u.id = :id")
    Optional<WalletUser> findByIdForUpdate(@Param("id") Long id);
}
