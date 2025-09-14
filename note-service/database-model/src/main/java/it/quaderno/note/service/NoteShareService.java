package it.quaderno.note.service;

import it.quaderno.note.service.dto.NoteShareDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link it.quaderno.note.domain.NoteShare}.
 */
public interface NoteShareService {
    /**
     * Save a noteShare.
     *
     * @param noteShareDTO the entity to save.
     * @return the persisted entity.
     */
    NoteShareDTO save(NoteShareDTO noteShareDTO);

    /**
     * Updates a noteShare.
     *
     * @param noteShareDTO the entity to update.
     * @return the persisted entity.
     */
    NoteShareDTO update(NoteShareDTO noteShareDTO);

    /**
     * Partially updates a noteShare.
     *
     * @param noteShareDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<NoteShareDTO> partialUpdate(NoteShareDTO noteShareDTO);

    /**
     * Get all the noteShares.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<NoteShareDTO> findAll(Pageable pageable);

    /**
     * Get the "id" noteShare.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<NoteShareDTO> findOne(Long id);

    /**
     * Delete the "id" noteShare.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
