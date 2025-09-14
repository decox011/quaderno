import note from 'app/entities/note/note.reducer';
import tag from 'app/entities/tag/tag.reducer';
import noteShare from 'app/entities/note-share/note-share.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  note,
  tag,
  noteShare,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
