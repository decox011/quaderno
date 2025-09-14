package it.quaderno.note.web.rest;

import static it.quaderno.note.domain.NoteShareAsserts.*;
import static it.quaderno.note.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.quaderno.note.IntegrationTest;
import it.quaderno.note.domain.NoteShare;
import it.quaderno.note.repository.NoteShareRepository;
import it.quaderno.note.repository.UserRepository;
import it.quaderno.note.repository.search.NoteShareSearchRepository;
import it.quaderno.note.service.dto.NoteShareDTO;
import it.quaderno.note.service.mapper.NoteShareMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link NoteShareResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class NoteShareResourceIT {

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/note-shares";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/note-shares/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private NoteShareRepository noteShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteShareMapper noteShareMapper;

    @Autowired
    private NoteShareSearchRepository noteShareSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restNoteShareMockMvc;

    private NoteShare noteShare;

    private NoteShare insertedNoteShare;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NoteShare createEntity() {
        return new NoteShare().createdAt(DEFAULT_CREATED_AT).updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NoteShare createUpdatedEntity() {
        return new NoteShare().createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    public void initTest() {
        noteShare = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedNoteShare != null) {
            noteShareRepository.delete(insertedNoteShare);
            noteShareSearchRepository.delete(insertedNoteShare);
            insertedNoteShare = null;
        }
    }

    @Test
    @Transactional
    void createNoteShare() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        // Create the NoteShare
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);
        var returnedNoteShareDTO = om.readValue(
            restNoteShareMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteShareDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            NoteShareDTO.class
        );

        // Validate the NoteShare in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedNoteShare = noteShareMapper.toEntity(returnedNoteShareDTO);
        assertNoteShareUpdatableFieldsEquals(returnedNoteShare, getPersistedNoteShare(returnedNoteShare));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedNoteShare = returnedNoteShare;
    }

    @Test
    @Transactional
    void createNoteShareWithExistingId() throws Exception {
        // Create the NoteShare with an existing ID
        noteShare.setId(1L);
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restNoteShareMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteShareDTO)))
            .andExpect(status().isBadRequest());

        // Validate the NoteShare in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        // set the field null
        noteShare.setCreatedAt(null);

        // Create the NoteShare, which fails.
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        restNoteShareMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteShareDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkUpdatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        // set the field null
        noteShare.setUpdatedAt(null);

        // Create the NoteShare, which fails.
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        restNoteShareMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteShareDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllNoteShares() throws Exception {
        // Initialize the database
        insertedNoteShare = noteShareRepository.saveAndFlush(noteShare);

        // Get all the noteShareList
        restNoteShareMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(noteShare.getId().intValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getNoteShare() throws Exception {
        // Initialize the database
        insertedNoteShare = noteShareRepository.saveAndFlush(noteShare);

        // Get the noteShare
        restNoteShareMockMvc
            .perform(get(ENTITY_API_URL_ID, noteShare.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(noteShare.getId().intValue()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingNoteShare() throws Exception {
        // Get the noteShare
        restNoteShareMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingNoteShare() throws Exception {
        // Initialize the database
        insertedNoteShare = noteShareRepository.saveAndFlush(noteShare);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        noteShareSearchRepository.save(noteShare);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());

        // Update the noteShare
        NoteShare updatedNoteShare = noteShareRepository.findById(noteShare.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedNoteShare are not directly saved in db
        em.detach(updatedNoteShare);
        updatedNoteShare.createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT);
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(updatedNoteShare);

        restNoteShareMockMvc
            .perform(
                put(ENTITY_API_URL_ID, noteShareDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(noteShareDTO))
            )
            .andExpect(status().isOk());

        // Validate the NoteShare in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedNoteShareToMatchAllProperties(updatedNoteShare);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<NoteShare> noteShareSearchList = Streamable.of(noteShareSearchRepository.findAll()).toList();
                NoteShare testNoteShareSearch = noteShareSearchList.get(searchDatabaseSizeAfter - 1);

                assertNoteShareAllPropertiesEquals(testNoteShareSearch, updatedNoteShare);
            });
    }

    @Test
    @Transactional
    void putNonExistingNoteShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        noteShare.setId(longCount.incrementAndGet());

        // Create the NoteShare
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNoteShareMockMvc
            .perform(
                put(ENTITY_API_URL_ID, noteShareDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(noteShareDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NoteShare in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchNoteShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        noteShare.setId(longCount.incrementAndGet());

        // Create the NoteShare
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNoteShareMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(noteShareDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NoteShare in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamNoteShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        noteShare.setId(longCount.incrementAndGet());

        // Create the NoteShare
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNoteShareMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(noteShareDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the NoteShare in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateNoteShareWithPatch() throws Exception {
        // Initialize the database
        insertedNoteShare = noteShareRepository.saveAndFlush(noteShare);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the noteShare using partial update
        NoteShare partialUpdatedNoteShare = new NoteShare();
        partialUpdatedNoteShare.setId(noteShare.getId());

        partialUpdatedNoteShare.updatedAt(UPDATED_UPDATED_AT);

        restNoteShareMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNoteShare.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNoteShare))
            )
            .andExpect(status().isOk());

        // Validate the NoteShare in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNoteShareUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedNoteShare, noteShare),
            getPersistedNoteShare(noteShare)
        );
    }

    @Test
    @Transactional
    void fullUpdateNoteShareWithPatch() throws Exception {
        // Initialize the database
        insertedNoteShare = noteShareRepository.saveAndFlush(noteShare);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the noteShare using partial update
        NoteShare partialUpdatedNoteShare = new NoteShare();
        partialUpdatedNoteShare.setId(noteShare.getId());

        partialUpdatedNoteShare.createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT);

        restNoteShareMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedNoteShare.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedNoteShare))
            )
            .andExpect(status().isOk());

        // Validate the NoteShare in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertNoteShareUpdatableFieldsEquals(partialUpdatedNoteShare, getPersistedNoteShare(partialUpdatedNoteShare));
    }

    @Test
    @Transactional
    void patchNonExistingNoteShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        noteShare.setId(longCount.incrementAndGet());

        // Create the NoteShare
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNoteShareMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, noteShareDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(noteShareDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NoteShare in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchNoteShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        noteShare.setId(longCount.incrementAndGet());

        // Create the NoteShare
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNoteShareMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(noteShareDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the NoteShare in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamNoteShare() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        noteShare.setId(longCount.incrementAndGet());

        // Create the NoteShare
        NoteShareDTO noteShareDTO = noteShareMapper.toDto(noteShare);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restNoteShareMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(noteShareDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the NoteShare in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteNoteShare() throws Exception {
        // Initialize the database
        insertedNoteShare = noteShareRepository.saveAndFlush(noteShare);
        noteShareRepository.save(noteShare);
        noteShareSearchRepository.save(noteShare);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the noteShare
        restNoteShareMockMvc
            .perform(delete(ENTITY_API_URL_ID, noteShare.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(noteShareSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchNoteShare() throws Exception {
        // Initialize the database
        insertedNoteShare = noteShareRepository.saveAndFlush(noteShare);
        noteShareSearchRepository.save(noteShare);

        // Search the noteShare
        restNoteShareMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + noteShare.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(noteShare.getId().intValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    protected long getRepositoryCount() {
        return noteShareRepository.count();
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

    protected NoteShare getPersistedNoteShare(NoteShare noteShare) {
        return noteShareRepository.findById(noteShare.getId()).orElseThrow();
    }

    protected void assertPersistedNoteShareToMatchAllProperties(NoteShare expectedNoteShare) {
        assertNoteShareAllPropertiesEquals(expectedNoteShare, getPersistedNoteShare(expectedNoteShare));
    }

    protected void assertPersistedNoteShareToMatchUpdatableProperties(NoteShare expectedNoteShare) {
        assertNoteShareAllUpdatablePropertiesEquals(expectedNoteShare, getPersistedNoteShare(expectedNoteShare));
    }
}
