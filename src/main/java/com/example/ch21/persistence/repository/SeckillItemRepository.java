package com.example.ch21.persistence.repository;

import com.example.ch21.persistence.entity.SeckillItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeckillItemRepository extends JpaRepository<SeckillItem, Integer> {
}
