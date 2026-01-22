package com.divyam.advent.repository;

import com.divyam.advent.model.TimeCapsule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for TimeCapsule entity.
 * Spring Data JPA automatically provides the implementation.
 */
@Repository
public interface TimeCapsuleRepository extends JpaRepository<TimeCapsule, Long> {

    /**
     * Find all capsules for a specific user.
     *
     * @param userId the ID of the user
     * @return list of all capsules for this user
     */
    List<TimeCapsule> findByUserId(Long userId);

    /**
     * Find capsules for a user that can be revealed (reveal date has passed).
     * Only returns capsules belonging to the specified user.
     *
     * @param userId the ID of the user
     * @param now the current date/time
     * @return list of revealable capsules for this user
     */
    @Query("SELECT tc FROM TimeCapsule tc WHERE tc.userId = :userId " +
           "AND tc.revealDate <= :now " +
           "ORDER BY tc.revealDate ASC")
    List<TimeCapsule> findRevealedCapsules(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Find pending (not yet revealed) capsules for a user.
     *
     * @param userId the ID of the user
     * @param now the current date/time
     * @return list of pending capsules for this user
     */
    @Query("SELECT tc FROM TimeCapsule tc WHERE tc.userId = :userId " +
           "AND tc.revealDate > :now " +
           "ORDER BY tc.revealDate ASC")
    List<TimeCapsule> findPendingCapsules(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
