import dayjs from 'dayjs';
import { INote } from 'app/shared/model/note.model';

export interface ITag {
  id?: number;
  name?: string;
  createdAt?: dayjs.Dayjs;
  updatedAt?: dayjs.Dayjs;
  notes?: INote[] | null;
}

export const defaultValue: Readonly<ITag> = {};
