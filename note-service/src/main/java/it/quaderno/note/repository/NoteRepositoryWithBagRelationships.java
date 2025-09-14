package it.quaderno.note.repository;

import it.quaderno.note.domain.Note;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface NoteRepositoryWithBagRelationships {
    Optional<Note> fetchBagRelationships(Optional<Note> note);

    List<Note> fetchBagRelationships(List<Note> notes);

    Page<Note> fetchBagRelationships(Page<Note> notes);
}
