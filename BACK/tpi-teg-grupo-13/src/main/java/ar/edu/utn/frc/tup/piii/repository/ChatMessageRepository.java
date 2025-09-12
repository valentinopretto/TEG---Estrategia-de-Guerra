package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.entities.ChatMessageEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByGame(GameEntity game);
    List<ChatMessageEntity> findBySender(PlayerEntity sender);
    List<ChatMessageEntity> findByGameAndIsSystemMessageTrue(GameEntity game);
    List<ChatMessageEntity> findByGameAndIsSystemMessageFalse(GameEntity game);

    List<ChatMessageEntity> findByGameIdOrderBySentAtAsc(Long gameId);
    List<ChatMessageEntity> findByGameIdAndIdGreaterThanOrderBySentAtAsc(Long gameId, Long sinceMessageId);

    @Query("SELECT cm FROM ChatMessageEntity cm WHERE cm.game = :game ORDER BY cm.sentAt DESC")
    List<ChatMessageEntity> findByGameOrderBySentAtDesc(@Param("game") GameEntity game);

    @Query("SELECT cm FROM ChatMessageEntity cm WHERE cm.game = :game AND cm.sentAt >= :since ORDER BY cm.sentAt ASC")
    List<ChatMessageEntity> findRecentMessagesByGame(@Param("game") GameEntity game, @Param("since") LocalDateTime since);

    @Query("SELECT cm FROM ChatMessageEntity cm WHERE cm.game = :game ORDER BY cm.sentAt DESC LIMIT :limit")
    List<ChatMessageEntity> findLastMessagesByGame(@Param("game") GameEntity game, @Param("limit") int limit);

    @Query("SELECT COUNT(cm) FROM ChatMessageEntity cm WHERE cm.sender = :player")
    Long countMessagesBySender(@Param("player") PlayerEntity player);
}