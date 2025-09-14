import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';
import { ITag } from 'app/shared/model/tag.model';

export interface INote {
  id?: number;
  title?: string;
  content?: string;
  links?: string | null;
  createdAt?: dayjs.Dayjs;
  updatedAt?: dayjs.Dayjs;
  owner?: IUser | null;
  tags?: ITag[] | null;
}

export const defaultValue: Readonly<INote> = {};
