package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.entities.GameSnapshotEntity;
import ar.edu.utn.frc.tup.piii.model.Game;
import java.util.List;
import java.util.Optional;

public interface GameSnapshotService {

    // CRUD básico
    GameSnapshotEntity save(GameSnapshotEntity snapshot);
    Optional<GameSnapshotEntity> findById(Long id);
    List<GameSnapshotEntity> findAll();
    List<GameSnapshotEntity> findByGame(Game game);
    void deleteById(Long id);

    // Creación y restauración de snapshots
    GameSnapshotEntity createSnapshot(Game game);
    GameSnapshotEntity createAutoSnapshot(Game game);
    void restoreFromSnapshot(Long gameId, Long snapshotId);
    GameSnapshotEntity getLatestSnapshot(Long gameId);

    // Gestión automática
    void autoSaveSnapshot(Game game);
    void scheduleAutoSave(Game game);
    void cleanOldSnapshots(Long gameId, int keepLast);

    // Validaciones
    boolean canRestoreSnapshot(Long gameId, Long snapshotId);
    boolean isSnapshotValid(GameSnapshotEntity snapshot);

    // Utilidades
    String serializeGameState(Game game);
    Game deserializeGameState(String serializedState);
    List<GameSnapshotEntity> getSnapshotHistory(Long gameId);
    int getSnapshotCount(Long gameId);
}
