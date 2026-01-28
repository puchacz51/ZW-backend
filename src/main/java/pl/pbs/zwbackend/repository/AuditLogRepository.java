package pl.pbs.zwbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.pbs.zwbackend.model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByEntity(String entity);
    
    List<AuditLog> findByEntityAndEntityId(String entity, Long entityId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByTimestampBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.timestamp DESC")
    List<AuditLog> findByAction(@Param("action") String action);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.entity = :entity AND a.action = :action")
    Long countByEntityAndAction(@Param("entity") String entity, @Param("action") String action);
}
