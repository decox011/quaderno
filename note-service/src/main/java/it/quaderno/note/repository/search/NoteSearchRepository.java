package it.quaderno.note.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import it.quaderno.note.domain.Note;
import it.quaderno.note.repository.NoteRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Note} entity.
 */
public interface NoteSearchRepository extends ElasticsearchRepository<Note, Long>, NoteSearchRepositoryInternal {}

interface NoteSearchRepositoryInternal {
    Page<Note> search(String query, Pageable pageable);

    Page<Note> search(Query query);

    @Async
    void index(Note entity);

    @Async
    void deleteFromIndexById(Long id);
}

class NoteSearchRepositoryInternalImpl implements NoteSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final NoteRepository repository;

    NoteSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, NoteRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Note> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Note> search(Query query) {
        SearchHits<Note> searchHits = elasticsearchTemplate.search(query, Note.class);
        List<Note> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Note entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Note.class);
    }
}
