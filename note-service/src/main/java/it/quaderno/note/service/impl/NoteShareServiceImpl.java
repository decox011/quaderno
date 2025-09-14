package it.quaderno.note.service.impl;

import it.quaderno.note.domain.NoteShare;
import it.quaderno.note.repository.NoteShareRepository;
import it.quaderno.note.repository.search.NoteShareSearchRepository;
import it.quaderno.note.service.NoteShareService;
import it.quaderno.note.service.dto.NoteShareDTO;
import it.quaderno.note.service.mapper.NoteShareMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link it.quaderno.note.domain.NoteShare}.
 */
@Service
@Transactional
public class NoteShareServiceImpl implements NoteShareService {

    private static final Logger LOG = LoggerFactory.getLogger(NoteShareServiceImpl.class);

    private final NoteShareRepository noteShareRepository;

    private final NoteShareMapper noteShareMapper;

    private final NoteShareSearchRepository noteShareSearchRepository;

    public NoteShareServiceImpl(
        NoteShareRepository noteShareRepository,
        NoteShareMapper noteShareMapper,
        NoteShareSearchRepository noteShareSearchRepository
    ) {
        this.noteShareRepository = noteShareRepository;
        this.noteShareMapper = noteShareMapper;
        this.noteShareSearchRepository = noteShareSearchRepository;
    }

    @Override
    public NoteShareDTO save(NoteShareDTO noteShareDTO) {
        LOG.debug("Request to save NoteShare : {}", noteShareDTO);
        NoteShare noteShare = noteShareMapper.toEntity(noteShareDTO);
        noteShare = noteShareRepository.save(noteShare);
        noteShareSearchRepository.index(noteShare);
        return noteShareMapper.toDto(noteShare);
    }

    @Override
    public NoteShareDTO update(NoteShareDTO noteShareDTO) {
        LOG.debug("Request to update NoteShare : {}", noteShareDTO);
        NoteShare noteShare = noteShareMapper.toEntity(noteShareDTO);
        noteShare = noteShareRepository.save(noteShare);
        noteShareSearchRepository.index(noteShare);
        return noteShareMapper.toDto(noteShare);
    }

    @Override
    public Optional<NoteShareDTO> partialUpdate(NoteShareDTO noteShareDTO) {
        LOG.debug("Request to partially update NoteShare : {}", noteShareDTO);

        return noteShareRepository
            .findById(noteShareDTO.getId())
            .map(existingNoteShare -> {
                noteShareMapper.partialUpdate(existingNoteShare, noteShareDTO);

                return existingNoteShare;
            })
            .map(noteShareRepository::save)
            .map(savedNoteShare -> {
                noteShareSearchRepository.index(savedNoteShare);
                return savedNoteShare;
            })
            .map(noteShareMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteShareDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all NoteShares");
        return noteShareRepository.findAll(pageable).map(noteShareMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NoteShareDTO> findOne(Long id) {
        LOG.debug("Request to get NoteShare : {}", id);
        return noteShareRepository.findById(id).map(noteShareMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete NoteShare : {}", id);
        noteShareRepository.deleteById(id);
        noteShareSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteShareDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of NoteShares for query {}", query);
        return noteShareSearchRepository.search(query, pageable).map(noteShareMapper::toDto);
    }
}
