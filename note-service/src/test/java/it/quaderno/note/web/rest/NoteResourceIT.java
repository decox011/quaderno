package it.quaderno.note.web.rest;

import static it.quaderno.note.domain.NoteAsserts.*;
import static it.quaderno.note.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.quaderno.note.IntegrationTest;
import it.quaderno.note.domain.Note;
import it.quaderno.note.repository.NoteRepository;
import it.quaderno.note.repository.UserRepository;
import it.quaderno.note.repository.search.NoteSearchRepository;
import it.quaderno.note.service.NoteService;
import it.quaderno.note.service.dto.NoteDTO;
import it.quaderno.note.service.mapper.NoteMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link NoteResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class NoteResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final String DEFAULT_LINKS = "AAAAAAAAAA";
    private static final String UPDATED_LINKS = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/notes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/notes/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private NoteRepository noteRepositoryMock;

    @Autowired
    private NoteMapper noteMapper;

    @Mock
    private NoteService noteServiceMock;

    @Autowired
    private NoteSearchRepository noteSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNoteMockMvc;

    private Note note;

    private Note insertedNote;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Note createEntity() {
        return new Note()
            .title(DEFAULT_TITLE)
            .content(DEFAULT_CONTENT)
            .links(DEFAULT_LINKS)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Note createUpdatedEntity() {
        return new Note()
            .title(UPDATED_TITLE)
            .content(UPDATED_CONTENT)
            .links(UPDATED_LINKS)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    public void initTest() {
        note = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedNote != null) {
            noteRepository.delete(insertedNote);
            noteSearchRepository.delete(insertedNote);
            insertedNote = null;
        }
    }

    @Test
    @Transactional
    void createNote() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        // Create the Note
        NoteDTO noteDTO = noteMapper.toDto(note);
        var returnedNoteDTO = om.readValue(
            restNoteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            NoteDTO.class
        );

        // Validate the Note in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedNote = noteMapper.toEntity(returnedNoteDTO);
        assertNoteUpdatableFieldsEquals(returnedNote, getPersistedNote(returnedNote));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedNote = returnedNote;
    }

    @Test
    @Transactional
    void createNoteWithExistingId() throws Exception {
        // Create the Note with an existing ID
        note.setId(1L);
        NoteDTO noteDTO = noteMapper.toDto(note);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restNoteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Note in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        // set the field null
        note.setTitle(null);

        // Create the Note, which fails.
        NoteDTO noteDTO = noteMapper.toDto(note);

        restNoteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        // set the field null
        note.setCreatedAt(null);

        // Create the Note, which fails.
        NoteDTO noteDTO = noteMapper.toDto(note);

        restNoteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkUpdatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        // set the field null
        note.setUpdatedAt(null);

        // Create the Note, which fails.
        NoteDTO noteDTO = noteMapper.toDto(note);

        restNoteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllNotes() throws Exception {
        // Initialize the database
        insertedNote = noteRepository.saveAndFlush(note);

        // Get all the noteList
        restNoteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(note.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].links").value(hasItem(DEFAULT_LINKS)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllNotesWithEagerRelationshipsIsEnabled() throws Exception {
        when(noteServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restNoteMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(noteServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllNotesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(noteServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restNoteMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(noteRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getNote() throws Exception {
        // Initialize the database
        insertedNote = noteRepository.saveAndFlush(note);

        // Get the note
        restNoteMockMvc
            .perform(get(ENTITY_API_URL_ID, note.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(note.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.links").value(DEFAULT_LINKS))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingNote() throws Exception {
        // Get the note
        restNoteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingNote() throws Exception {
        // Initialize the database
        insertedNote = noteRepository.saveAndFlush(note);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        noteSearchRepository.save(note);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());

        // Update the note
        Note updatedNote = noteRepository.findById(note.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedNote are not directly saved in db
        em.detach(updatedNote);
        updatedNote
            .title(UPDATED_TITLE)
            .content(UPDATED_CONTENT)
            .links(UPDATED_LINKS)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        NoteDTO noteDTO = noteMapper.toDto(updatedNote);

        restNoteMockMvc
            .perform(put(ENTITY_API_URL_ID, noteDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteDTO)))
            .andExpect(status().isOk());

        // Validate the Note in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedNoteToMatchAllProperties(updatedNote);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Note> noteSearchList = Streamable.of(noteSearchRepository.findAll()).toList();
                Note testNoteSearch = noteSearchList.get(searchDatabaseSizeAfter - 1);

                assertNoteAllPropertiesEquals(testNoteSearch, updatedNote);
            });
    }

    @Test
    @Transactional
    void putNonExistingNote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        note.setId(longCount.incrementAndGet());

        // Create the Note
        NoteDTO noteDTO = noteMapper.toDto(note);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNoteMockMvc
            .perform(put(ENTITY_API_URL_ID, noteDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Note in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchNote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        note.setId(longCount.incrementAndGet());

        // Create the Note
        NoteDTO noteDTO = noteMapper.toDto(note);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNoteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(noteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Note in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamNote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        note.setId(longCount.incrementAndGet());

        // Create the Note
        NoteDTO noteDTO = noteMapper.toDto(note);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNoteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Note in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateNoteWithPatch() throws Exception {
        // Initialize the database
        insertedNote = noteRepository.saveAndFlush(note);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the note using partial update
        Note partialUpdatedNote = new Note();
        partialUpdatedNote.setId(note.getId());

        partialUpdatedNote.content(UPDATED_CONTENT).createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT);

        restNoteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNote.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNote))
            )
            .andExpect(status().isOk());

        // Validate the Note in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNoteUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedNote, note), getPersistedNote(note));
    }

    @Test
    @Transactional
    void fullUpdateNoteWithPatch() throws Exception {
        // Initialize the database
        insertedNote = noteRepository.saveAndFlush(note);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the note using partial update
        Note partialUpdatedNote = new Note();
        partialUpdatedNote.setId(note.getId());

        partialUpdatedNote
            .title(UPDATED_TITLE)
            .content(UPDATED_CONTENT)
            .links(UPDATED_LINKS)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restNoteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNote.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNote))
            )
            .andExpect(status().isOk());

        // Validate the Note in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNoteUpdatableFieldsEquals(partialUpdatedNote, getPersistedNote(partialUpdatedNote));
    }

    @Test
    @Transactional
    void patchNonExistingNote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        note.setId(longCount.incrementAndGet());

        // Create the Note
        NoteDTO noteDTO = noteMapper.toDto(note);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNoteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, noteDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(noteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Note in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchNote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        note.setId(longCount.incrementAndGet());

        // Create the Note
        NoteDTO noteDTO = noteMapper.toDto(note);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNoteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(noteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Note in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamNote() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        note.setId(longCount.incrementAndGet());

        // Create the Note
        NoteDTO noteDTO = noteMapper.toDto(note);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNoteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(noteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Note in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteNote() throws Exception {
        // Initialize the database
        insertedNote = noteRepository.saveAndFlush(note);
        noteRepository.save(note);
        noteSearchRepository.save(note);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the note
        restNoteMockMvc
            .perform(delete(ENTITY_API_URL_ID, note.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchNote() throws Exception {
        // Initialize the database
        insertedNote = noteRepository.saveAndFlush(note);
        noteSearchRepository.save(note);

        // Search the note
        restNoteMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + note.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(note.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].links").value(hasItem(DEFAULT_LINKS.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    protected long getRepositoryCount() {
        return noteRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Note getPersistedNote(Note note) {
        return noteRepository.findById(note.getId()).orElseThrow();
    }

    protected void assertPersistedNoteToMatchAllProperties(Note expectedNote) {
        assertNoteAllPropertiesEquals(expectedNote, getPersistedNote(expectedNote));
    }

    protected void assertPersistedNoteToMatchUpdatableProperties(Note expectedNote) {
        assertNoteAllUpdatablePropertiesEquals(expectedNote, getPersistedNote(expectedNote));
    }
}
