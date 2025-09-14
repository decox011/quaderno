package it.quaderno.note.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link it.quaderno.note.domain.NoteShare} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NoteShareDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;

    private UserDTO sharedWith;

    private NoteDTO note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserDTO getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(UserDTO sharedWith) {
        this.sharedWith = sharedWith;
    }

    public NoteDTO getNote() {
        return note;
    }

    public void setNote(NoteDTO note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NoteShareDTO)) {
            return false;
        }

        NoteShareDTO noteShareDTO = (NoteShareDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, noteShareDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NoteShareDTO{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", sharedWith=" + getSharedWith() +
            ", note=" + getNote() +
            "}";
    }
}
