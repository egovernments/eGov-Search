package org.egov.search.service;

import org.apache.commons.lang.StringUtils;
import org.egov.search.domain.Filter;
import org.egov.search.domain.Filters;
import org.egov.search.domain.Page;
import org.egov.search.domain.SearchResult;
import org.egov.search.domain.Sort;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class SearchService {

    private ElasticSearchClient elasticSearchClient;

    @Autowired
    public SearchService(ElasticSearchClient elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
    }

    public SearchResult search(List<String> indices, List<String> types, String searchText, Filters filters, Sort sort, Page page) {
        FilterBuilder filterBuilder = constructBoolFilter(filters);

        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        if(StringUtils.isNotEmpty(searchText)) {
            queryBuilder = QueryBuilders.queryString(searchText)
                    .lenient(true)
                    .field("searchable.*")
                    .field("common.*")
            		.field("clauses.*");
        }

        QueryBuilder rootQueryBuilder;
        if (filters.isNotEmpty()) {
            rootQueryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);
        } else {
            rootQueryBuilder = queryBuilder;
        }

        String response = elasticSearchClient.search(indices, types, rootQueryBuilder, sort, page);
        return SearchResult.from(response);
    }

    private BoolFilterBuilder constructBoolFilter(Filters filters) {
        List<FilterBuilder> mustFilters = queryBuilders(filters.getAndFilters());
        List<FilterBuilder> shouldFilters = queryBuilders(filters.getOrFilters());
        List<FilterBuilder> notFilters = queryBuilders(filters.getNotInFilters());

        BoolFilterBuilder boolFilterBuilder = FilterBuilders.boolFilter();
        mustFilters.stream().forEach(boolFilterBuilder::must);
        shouldFilters.stream().forEach(boolFilterBuilder::should);
        notFilters.stream().forEach(boolFilterBuilder::mustNot);

        return boolFilterBuilder;
    }

    private List<FilterBuilder> queryBuilders(List<Filter> filters) {
        return filters.stream()
                .map(Filter::filterBuilder)
                .collect(toList());
    }

}
