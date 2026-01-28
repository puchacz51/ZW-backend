package pl.pbs.zwbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.pbs.zwbackend.model.TaskComment;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    
    List<TaskComment> findByTaskId(Long taskId);
    
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    List<TaskComment> findByUserId(Long userId);
    
    @Query("SELECT COUNT(tc) FROM TaskComment tc WHERE tc.task.id = :taskId")
    Long countByTaskId(@Param("taskId") Long taskId);
    
    @Query("SELECT tc FROM TaskComment tc JOIN FETCH tc.user WHERE tc.task.id = :taskId ORDER BY tc.createdAt DESC")
    List<TaskComment> findByTaskIdWithUsers(@Param("taskId") Long taskId);
    
    void deleteByTaskId(Long taskId);
}
