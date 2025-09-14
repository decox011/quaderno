package it.quaderno.note.service.mapper;

import static it.quaderno.note.domain.NoteAsserts.*;
import static it.quaderno.note.domain.NoteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoteMapperTest {

    private NoteMapper noteMapper;

    @BeforeEach
    void setUp() {
        noteMapper = new NoteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getNoteSample1();
        var actual = noteMapper.toEntity(noteMapper.toDto(expected));
        assertNoteAllPropertiesEquals(expected, actual);
    }
}
