package it.quaderno.note.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import it.quaderno.note.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class NoteShareDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(NoteShareDTO.class);
        NoteShareDTO noteShareDTO1 = new NoteShareDTO();
        noteShareDTO1.setId(1L);
        NoteShareDTO noteShareDTO2 = new NoteShareDTO();
        assertThat(noteShareDTO1).isNotEqualTo(noteShareDTO2);
        noteShareDTO2.setId(noteShareDTO1.getId());
        assertThat(noteShareDTO1).isEqualTo(noteShareDTO2);
        noteShareDTO2.setId(2L);
        assertThat(noteShareDTO1).isNotEqualTo(noteShareDTO2);
        noteShareDTO1.setId(null);
        assertThat(noteShareDTO1).isNotEqualTo(noteShareDTO2);
    }
}
