package it.quaderno.note.service.mapper;

import it.quaderno.note.domain.Note;
import it.quaderno.note.domain.Tag;
import it.quaderno.note.domain.User;
import it.quaderno.note.service.dto.NoteDTO;
import it.quaderno.note.service.dto.TagDTO;
import it.quaderno.note.service.dto.UserDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Note} and its DTO {@link NoteDTO}.
 */
@Mapper(componentModel = "spring")
public interface NoteMapper extends EntityMapper<NoteDTO, Note> {
    @Mapping(target = "owner", source = "owner", qualifiedByName = "userId")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagNameSet")
    NoteDTO toDto(Note s);

    @Mapping(target = "removeTags", ignore = true)
    Note toEntity(NoteDTO noteDTO);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);

    @Named("tagName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TagDTO toDtoTagName(Tag tag);

    @Named("tagNameSet")
    default Set<TagDTO> toDtoTagNameSet(Set<Tag> tag) {
        return tag.stream().map(this::toDtoTagName).collect(Collectors.toSet());
    }
}
