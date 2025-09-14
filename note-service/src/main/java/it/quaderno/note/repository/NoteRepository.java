package it.quaderno.note.repository;

import it.quaderno.note.domain.Note;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Note entity.
 *
 * When extending this class, extend NoteRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface NoteRepository extends NoteRepositoryWithBagRelationships, JpaRepository<Note, Long> {
    @Query("select note from Note note where note.owner.login = ?#{authentication.name}")
    List<Note> findByOwnerIsCurrentUser();

    default Optional<Note> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findById(id));
    }

    default List<Note> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAll());
    }

    default Page<Note> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAll(pageable));
    }

    @Query("select distinct n " +
            "from Note n " +
            "left join fetch n.tags " +
            "left join fetch n.noteShares s " +
            "left join s.sharedWith sw " +
            "where n.owner.login = :login or sw.login = :login")
    Page<Note> findAllAccessibleByUser(Pageable pageable, @Param("login") String login);
}
