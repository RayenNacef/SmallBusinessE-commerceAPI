package com.smallshop.inventory.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface StockItemJpaRepository extends JpaRepository<StockItemJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItemJpaEntity s where s.productId in :productIds order by s.productId")
    List<StockItemJpaEntity> findAllForUpdate(@Param("productIds") Collection<UUID> productIds);
}
