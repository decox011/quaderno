import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './note-share.reducer';

export const NoteShareDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const noteShareEntity = useAppSelector(state => state.noteShare.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="noteShareDetailsHeading">
          <Translate contentKey="noteServiceApp.noteShare.detail.title">NoteShare</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{noteShareEntity.id}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="noteServiceApp.noteShare.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>
            {noteShareEntity.createdAt ? <TextFormat value={noteShareEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="noteServiceApp.noteShare.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>
            {noteShareEntity.updatedAt ? <TextFormat value={noteShareEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <Translate contentKey="noteServiceApp.noteShare.sharedWith">Shared With</Translate>
          </dt>
          <dd>{noteShareEntity.sharedWith ? noteShareEntity.sharedWith.id : ''}</dd>
          <dt>
            <Translate contentKey="noteServiceApp.noteShare.note">Note</Translate>
          </dt>
          <dd>{noteShareEntity.note ? noteShareEntity.note.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/note-share" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/note-share/${noteShareEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default NoteShareDetail;
