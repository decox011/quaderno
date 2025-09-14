package it.quaderno.note.domain;

import static it.quaderno.note.domain.NoteShareTestSamples.*;
import static it.quaderno.note.domain.NoteTestSamples.*;
import static it.quaderno.note.domain.TagTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import it.quaderno.note.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NoteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Note.class);
        Note note1 = getNoteSample1();
        Note note2 = new Note();
        assertThat(note1).isNotEqualTo(note2);

        note2.setId(note1.getId());
        assertThat(note1).isEqualTo(note2);

        note2 = getNoteSample2();
        assertThat(note1).isNotEqualTo(note2);
    }

    @Test
    void noteShareTest() {
        Note note = getNoteRandomSampleGenerator();
        NoteShare noteShareBack = getNoteShareRandomSampleGenerator();

        note.addNoteShare(noteShareBack);
        assertThat(note.getNoteShares()).containsOnly(noteShareBack);
        assertThat(noteShareBack.getNote()).isEqualTo(note);

        note.removeNoteShare(noteShareBack);
        assertThat(note.getNoteShares()).doesNotContain(noteShareBack);
        assertThat(noteShareBack.getNote()).isNull();

        note.noteShares(new HashSet<>(Set.of(noteShareBack)));
        assertThat(note.getNoteShares()).containsOnly(noteShareBack);
        assertThat(noteShareBack.getNote()).isEqualTo(note);

        note.setNoteShares(new HashSet<>());
        assertThat(note.getNoteShares()).doesNotContain(noteShareBack);
        assertThat(noteShareBack.getNote()).isNull();
    }

    @Test
    void tagsTest() {
        Note note = getNoteRandomSampleGenerator();
        Tag tagBack = getTagRandomSampleGenerator();

        note.addTags(tagBack);
        assertThat(note.getTags()).containsOnly(tagBack);

        note.removeTags(tagBack);
        assertThat(note.getTags()).doesNotContain(tagBack);

        note.tags(new HashSet<>(Set.of(tagBack)));
        assertThat(note.getTags()).containsOnly(tagBack);

        note.setTags(new HashSet<>());
        assertThat(note.getTags()).doesNotContain(tagBack);
    }
}
