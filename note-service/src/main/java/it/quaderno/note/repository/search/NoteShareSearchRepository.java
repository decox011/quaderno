package it.quaderno.note.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import it.quaderno.note.domain.NoteShare;
import it.quaderno.note.repository.NoteShareRepository;
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
 * Spring Data Elasticsearch repository for the {@link NoteShare} entity.
 */
public interface NoteShareSearchRepository extends ElasticsearchRepository<NoteShare, Long>, NoteShareSearchRepositoryInternal {}

interface NoteShareSearchRepositoryInternal {
    Page<NoteShare> search(String query, Pageable pageable);

    Page<NoteShare> search(Query query);

    @Async
    void index(NoteShare entity);

    @Async
    void deleteFromIndexById(Long id);
}

class NoteShareSearchRepositoryInternalImpl implements NoteShareSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final NoteShareRepository repository;

    NoteShareSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, NoteShareRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<NoteShare> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<NoteShare> search(Query query) {
        SearchHits<NoteShare> searchHits = elasticsearchTemplate.search(query, NoteShare.class);
        List<NoteShare> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(NoteShare entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), NoteShare.class);
    }
}
