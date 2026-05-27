package com.divyam.advent.repository;

import com.divyam.advent.model.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {

    List<Prize> findAllByOrderByCreatedAtDesc();

    List<Prize> findByActiveTrueOrderByCreatedAtDesc();

    List<Prize> findByAwardedUserIdOrderByAwardedAtDesc(Long awardedUserId);
}
