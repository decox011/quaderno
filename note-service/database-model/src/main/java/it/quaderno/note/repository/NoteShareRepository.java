package it.quaderno.note.repository;

import it.quaderno.note.domain.NoteShare;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NoteShare entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NoteShareRepository extends JpaRepository<NoteShare, Long> {
    @Query("select noteShare from NoteShare noteShare where noteShare.sharedWith.login = ?#{authentication.name}")
    List<NoteShare> findBySharedWithIsCurrentUser();
}
