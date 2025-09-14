import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';
import { INote } from 'app/shared/model/note.model';

export interface INoteShare {
  id?: number;
  createdAt?: dayjs.Dayjs;
  updatedAt?: dayjs.Dayjs;
  sharedWith?: IUser | null;
  note?: INote | null;
}

export const defaultValue: Readonly<INoteShare> = {};
