package it.quaderno.note.service.mapper;

import it.quaderno.note.domain.Note;
import it.quaderno.note.domain.NoteShare;
import it.quaderno.note.domain.User;
import it.quaderno.note.service.dto.NoteDTO;
import it.quaderno.note.service.dto.NoteShareDTO;
import it.quaderno.note.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link NoteShare} and its DTO {@link NoteShareDTO}.
 */
@Mapper(componentModel = "spring")
public interface NoteShareMapper extends EntityMapper<NoteShareDTO, NoteShare> {
    @Mapping(target = "sharedWith", source = "sharedWith", qualifiedByName = "userId")
    @Mapping(target = "note", source = "note", qualifiedByName = "noteId")
    NoteShareDTO toDto(NoteShare s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);

    @Named("noteId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    NoteDTO toDtoNoteId(Note note);
}
