package it.quaderno.note.service.mapper;

import static it.quaderno.note.domain.NoteShareAsserts.*;
import static it.quaderno.note.domain.NoteShareTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoteShareMapperTest {

    private NoteShareMapper noteShareMapper;

    @BeforeEach
    void setUp() {
        noteShareMapper = new NoteShareMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getNoteShareSample1();
        var actual = noteShareMapper.toEntity(noteShareMapper.toDto(expected));
        assertNoteShareAllPropertiesEquals(expected, actual);
    }
}
