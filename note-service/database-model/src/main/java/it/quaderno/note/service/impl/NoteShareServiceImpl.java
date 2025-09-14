package it.quaderno.note.service.impl;

import it.quaderno.note.domain.NoteShare;
import it.quaderno.note.repository.NoteShareRepository;
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

    public NoteShareServiceImpl(NoteShareRepository noteShareRepository, NoteShareMapper noteShareMapper) {
        this.noteShareRepository = noteShareRepository;
        this.noteShareMapper = noteShareMapper;
    }

    @Override
    public NoteShareDTO save(NoteShareDTO noteShareDTO) {
        LOG.debug("Request to save NoteShare : {}", noteShareDTO);
        NoteShare noteShare = noteShareMapper.toEntity(noteShareDTO);
        noteShare = noteShareRepository.save(noteShare);
        return noteShareMapper.toDto(noteShare);
    }

    @Override
    public NoteShareDTO update(NoteShareDTO noteShareDTO) {
        LOG.debug("Request to update NoteShare : {}", noteShareDTO);
        NoteShare noteShare = noteShareMapper.toEntity(noteShareDTO);
        noteShare = noteShareRepository.save(noteShare);
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
    }
}
