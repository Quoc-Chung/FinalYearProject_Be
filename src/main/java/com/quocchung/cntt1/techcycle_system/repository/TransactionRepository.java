package com.quocchung.cntt1.techcycle_system.repository;

import com.quocchung.cntt1.techcycle_system.model.Transaction;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByPostPostId(Long postId);

    Page<Transaction> findBySellerUserIdOrBuyerUserIdOrderByCreatedAtDesc(
        Long sellerId, Long buyerId, Pageable pageable);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t " +
           "WHERE ((t.seller.userId = :userId1 AND t.buyer.userId = :userId2) " +
           "   OR (t.seller.userId = :userId2 AND t.buyer.userId = :userId1)) " +
           "AND t.status = 'COMPLETED'")
    boolean existsCompletedTransactionBetweenUsers(
        @Param("userId1") Long userId1,
        @Param("userId2") Long userId2);

    @Query("SELECT t FROM Transaction t " +
           "WHERE ((t.seller.userId = :userId1 AND t.buyer.userId = :userId2) " +
           "   OR (t.seller.userId = :userId2 AND t.buyer.userId = :userId1)) " +
           "AND t.status = 'COMPLETED' " +
           "ORDER BY t.createdAt DESC")
    Optional<Transaction> findCompletedTransactionBetweenUsers(
        @Param("userId1") Long userId1,
        @Param("userId2") Long userId2);
}
