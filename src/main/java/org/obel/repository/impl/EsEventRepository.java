package org.obel.repository.impl;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.obel.model.Event;
import org.obel.repository.EventRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EsEventRepository extends EsCrudRepository<Event> implements EventRepository{

    public static String TYPE = "event";
    public static String INDEX_NAME = "test";

    public EsEventRepository(Client client) {
        super(client, TYPE, INDEX_NAME);
    }

    public List<Event> search(String keyword) {
        final MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder(keyword, "city", "name", "category", "description", "zipCode", "address");
        final SearchResponse searchResponse = client.prepareSearch(indexName).setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(multiMatchQueryBuilder)
                .setSize(40)
                .setExplain(true).execute().actionGet();
        final List<Event> events = Stream.of(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsString)
                .filter(Objects::nonNull)
                .map(this::toItem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return events;

    }
}
