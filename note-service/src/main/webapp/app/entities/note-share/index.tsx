import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import NoteShare from './note-share';
import NoteShareDetail from './note-share-detail';
import NoteShareUpdate from './note-share-update';
import NoteShareDeleteDialog from './note-share-delete-dialog';

const NoteShareRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<NoteShare />} />
    <Route path="new" element={<NoteShareUpdate />} />
    <Route path=":id">
      <Route index element={<NoteShareDetail />} />
      <Route path="edit" element={<NoteShareUpdate />} />
      <Route path="delete" element={<NoteShareDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default NoteShareRoutes;
