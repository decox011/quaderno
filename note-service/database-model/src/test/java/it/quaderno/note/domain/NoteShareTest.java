package it.quaderno.note.domain;

import static it.quaderno.note.domain.NoteShareTestSamples.*;
import static it.quaderno.note.domain.NoteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import it.quaderno.note.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class NoteShareTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(NoteShare.class);
        NoteShare noteShare1 = getNoteShareSample1();
        NoteShare noteShare2 = new NoteShare();
        assertThat(noteShare1).isNotEqualTo(noteShare2);

        noteShare2.setId(noteShare1.getId());
        assertThat(noteShare1).isEqualTo(noteShare2);

        noteShare2 = getNoteShareSample2();
        assertThat(noteShare1).isNotEqualTo(noteShare2);
    }

    @Test
    void noteTest() {
        NoteShare noteShare = getNoteShareRandomSampleGenerator();
        Note noteBack = getNoteRandomSampleGenerator();

        noteShare.setNote(noteBack);
        assertThat(noteShare.getNote()).isEqualTo(noteBack);

        noteShare.note(null);
        assertThat(noteShare.getNote()).isNull();
    }
}
