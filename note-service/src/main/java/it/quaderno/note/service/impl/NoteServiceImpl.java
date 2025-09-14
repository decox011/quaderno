package it.quaderno.note.service.impl;

import it.quaderno.note.domain.Note;
import it.quaderno.note.repository.NoteRepository;
import it.quaderno.note.repository.search.NoteSearchRepository;
import it.quaderno.note.service.NoteService;
import it.quaderno.note.service.dto.NoteDTO;
import it.quaderno.note.service.mapper.NoteMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link it.quaderno.note.domain.Note}.
 */
@Service
@Transactional
public class NoteServiceImpl implements NoteService {

    private static final Logger LOG = LoggerFactory.getLogger(NoteServiceImpl.class);

    private final NoteRepository noteRepository;

    private final NoteMapper noteMapper;

    private final NoteSearchRepository noteSearchRepository;

    public NoteServiceImpl(NoteRepository noteRepository, NoteMapper noteMapper, NoteSearchRepository noteSearchRepository) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
        this.noteSearchRepository = noteSearchRepository;
    }

    @Override
    public NoteDTO save(NoteDTO noteDTO) {
        LOG.debug("Request to save Note : {}", noteDTO);
        Note note = noteMapper.toEntity(noteDTO);
        note = noteRepository.save(note);
        noteSearchRepository.index(note);
        return noteMapper.toDto(note);
    }

    @Override
    public NoteDTO update(NoteDTO noteDTO) {
        LOG.debug("Request to update Note : {}", noteDTO);
        Note note = noteMapper.toEntity(noteDTO);
        note = noteRepository.save(note);
        noteSearchRepository.index(note);
        return noteMapper.toDto(note);
    }

    @Override
    public Optional<NoteDTO> partialUpdate(NoteDTO noteDTO) {
        LOG.debug("Request to partially update Note : {}", noteDTO);

        return noteRepository
            .findById(noteDTO.getId())
            .map(existingNote -> {
                noteMapper.partialUpdate(existingNote, noteDTO);

                return existingNote;
            })
            .map(noteRepository::save)
            .map(savedNote -> {
                noteSearchRepository.index(savedNote);
                return savedNote;
            })
            .map(noteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Notes");
        return noteRepository.findAll(pageable).map(noteMapper::toDto);
    }

    public Page<NoteDTO> findAllWithEagerRelationships(Pageable pageable) {
        return noteRepository.findAllWithEagerRelationships(pageable).map(noteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NoteDTO> findOne(Long id) {
        LOG.debug("Request to get Note : {}", id);
        return noteRepository.findOneWithEagerRelationships(id).map(noteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Note : {}", id);
        noteRepository.deleteById(id);
        noteSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Notes for query {}", query);
        return noteSearchRepository.search(query, pageable).map(noteMapper::toDto);
    }
}
