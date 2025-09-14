package it.quaderno.note.web.rest;

import it.quaderno.note.repository.NoteShareRepository;
import it.quaderno.note.service.NoteShareService;
import it.quaderno.note.service.dto.NoteShareDTO;
import it.quaderno.note.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link it.quaderno.note.domain.NoteShare}.
 */
@RestController
@RequestMapping("/api/note-shares")
public class NoteShareResource {

    private static final Logger LOG = LoggerFactory.getLogger(NoteShareResource.class);

    private static final String ENTITY_NAME = "noteShare";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final NoteShareService noteShareService;

    private final NoteShareRepository noteShareRepository;

    public NoteShareResource(NoteShareService noteShareService, NoteShareRepository noteShareRepository) {
        this.noteShareService = noteShareService;
        this.noteShareRepository = noteShareRepository;
    }

    /**
     * {@code POST  /note-shares} : Create a new noteShare.
     *
     * @param noteShareDTO the noteShareDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new noteShareDTO, or with status {@code 400 (Bad Request)} if the noteShare has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NoteShareDTO> createNoteShare(@Valid @RequestBody NoteShareDTO noteShareDTO) throws URISyntaxException {
        LOG.debug("REST request to save NoteShare : {}", noteShareDTO);
        if (noteShareDTO.getId() != null) {
            throw new BadRequestAlertException("A new noteShare cannot already have an ID", ENTITY_NAME, "idexists");
        }
        noteShareDTO = noteShareService.save(noteShareDTO);
        return ResponseEntity.created(new URI("/api/note-shares/" + noteShareDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, noteShareDTO.getId().toString()))
            .body(noteShareDTO);
    }

    /**
     * {@code PUT  /note-shares/:id} : Updates an existing noteShare.
     *
     * @param id the id of the noteShareDTO to save.
     * @param noteShareDTO the noteShareDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated noteShareDTO,
     * or with status {@code 400 (Bad Request)} if the noteShareDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the noteShareDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteShareDTO> updateNoteShare(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody NoteShareDTO noteShareDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update NoteShare : {}, {}", id, noteShareDTO);
        if (noteShareDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, noteShareDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!noteShareRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        noteShareDTO = noteShareService.update(noteShareDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, noteShareDTO.getId().toString()))
            .body(noteShareDTO);
    }

    /**
     * {@code PATCH  /note-shares/:id} : Partial updates given fields of an existing noteShare, field will ignore if it is null
     *
     * @param id the id of the noteShareDTO to save.
     * @param noteShareDTO the noteShareDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated noteShareDTO,
     * or with status {@code 400 (Bad Request)} if the noteShareDTO is not valid,
     * or with status {@code 404 (Not Found)} if the noteShareDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the noteShareDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NoteShareDTO> partialUpdateNoteShare(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody NoteShareDTO noteShareDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update NoteShare partially : {}, {}", id, noteShareDTO);
        if (noteShareDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, noteShareDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!noteShareRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NoteShareDTO> result = noteShareService.partialUpdate(noteShareDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, noteShareDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /note-shares} : get all the noteShares.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of noteShares in body.
     */
    @GetMapping("")
    public ResponseEntity<List<NoteShareDTO>> getAllNoteShares(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of NoteShares");
        Page<NoteShareDTO> page = noteShareService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /note-shares/:id} : get the "id" noteShare.
     *
     * @param id the id of the noteShareDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the noteShareDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteShareDTO> getNoteShare(@PathVariable("id") Long id) {
        LOG.debug("REST request to get NoteShare : {}", id);
        Optional<NoteShareDTO> noteShareDTO = noteShareService.findOne(id);
        return ResponseUtil.wrapOrNotFound(noteShareDTO);
    }

    /**
     * {@code DELETE  /note-shares/:id} : delete the "id" noteShare.
     *
     * @param id the id of the noteShareDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteShare(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete NoteShare : {}", id);
        noteShareService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
