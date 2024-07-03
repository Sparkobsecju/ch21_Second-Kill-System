package com.example.ch21.persistence.repository;

import com.example.ch21.persistence.entity.SeckillOrder;
import com.example.ch21.persistence.entity.SeckillOrderPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, SeckillOrderPK>{
}
