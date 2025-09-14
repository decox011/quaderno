import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './note.reducer';

export const NoteDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const noteEntity = useAppSelector(state => state.note.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="noteDetailsHeading">
          <Translate contentKey="noteServiceApp.note.detail.title">Note</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{noteEntity.id}</dd>
          <dt>
            <span id="title">
              <Translate contentKey="noteServiceApp.note.title">Title</Translate>
            </span>
          </dt>
          <dd>{noteEntity.title}</dd>
          <dt>
            <span id="content">
              <Translate contentKey="noteServiceApp.note.content">Content</Translate>
            </span>
          </dt>
          <dd>{noteEntity.content}</dd>
          <dt>
            <span id="links">
              <Translate contentKey="noteServiceApp.note.links">Links</Translate>
            </span>
          </dt>
          <dd>{noteEntity.links}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="noteServiceApp.note.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>{noteEntity.createdAt ? <TextFormat value={noteEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="noteServiceApp.note.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>{noteEntity.updatedAt ? <TextFormat value={noteEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="noteServiceApp.note.owner">Owner</Translate>
          </dt>
          <dd>{noteEntity.owner ? noteEntity.owner.id : ''}</dd>
          <dt>
            <Translate contentKey="noteServiceApp.note.tags">Tags</Translate>
          </dt>
          <dd>
            {noteEntity.tags
              ? noteEntity.tags.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.name}</a>
                    {noteEntity.tags && i === noteEntity.tags.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button tag={Link} to="/note" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/note/${noteEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default NoteDetail;
