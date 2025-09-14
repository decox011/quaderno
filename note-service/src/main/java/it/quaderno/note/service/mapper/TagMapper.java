package it.quaderno.note.service.mapper;

import it.quaderno.note.domain.Note;
import it.quaderno.note.domain.Tag;
import it.quaderno.note.service.dto.NoteDTO;
import it.quaderno.note.service.dto.TagDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Tag} and its DTO {@link TagDTO}.
 */
@Mapper(componentModel = "spring")
public interface TagMapper extends EntityMapper<TagDTO, Tag> {
    @Mapping(target = "notes", source = "notes", qualifiedByName = "noteTitleSet")
    TagDTO toDto(Tag s);

    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "removeNotes", ignore = true)
    Tag toEntity(TagDTO tagDTO);

    @Named("noteTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    NoteDTO toDtoNoteTitle(Note note);

    @Named("noteTitleSet")
    default Set<NoteDTO> toDtoNoteTitleSet(Set<Note> note) {
        return note.stream().map(this::toDtoNoteTitle).collect(Collectors.toSet());
    }
}
