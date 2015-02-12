package org.egov.search.service;

import org.egov.search.domain.Filters;
import org.egov.search.domain.Page;
import org.egov.search.domain.SearchResult;
import org.egov.search.domain.Sort;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.egov.search.domain.Filters.withAndFilters;
import static org.egov.search.domain.Filters.withAndPlusNotFilters;
import static org.egov.search.domain.Filters.withAndPlusOrFilters;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SearchServiceMultipleFiltersTest extends SearchServiceTest {

    @Test
    public void shouldSearchWithEmptyFilters() {
        SearchResult searchResult = searchService.search(asList(indexName), asList(), Filters.NULL, Sort.NULL, Page.NULL);

        assertThat(searchResult.documentCount(), is(11));
    }

    @Test
    public void shouldSearchWithSingleFilter() {
        Map<String, String> andFilters = new HashMap<>();
        andFilters.put("clauses.mode", "INTERNET");

        SearchResult searchResult = searchService.search(asList(indexName), asList(), Filters.withAndFilters(andFilters), Sort.NULL, Page.NULL);

        assertThat(searchResult.documentCount(), is(3));
        assertThat(complaintNumbers(searchResult), containsInAnyOrder("299DIF", "751HFP", "696IDN"));
    }

    @Test
    public void shouldSearchWithMultipleFilters() {
        Map<String, String> andFilters = new HashMap<>();
        andFilters.put("clauses.mode", "INTERNET");
        andFilters.put("clauses.status", "REGISTERED");

        SearchResult searchResult = searchService.search(asList(indexName), asList(), Filters.withAndFilters(andFilters), Sort.NULL, Page.NULL);

        assertThat(searchResult.documentCount(), is(2));
        assertThat(complaintNumbers(searchResult), containsInAnyOrder("751HFP", "696IDN"));
    }

    @Test
    public void shouldSearchWithMultipleFiltersIncludingPartialMatch() {
        Map<String, String> andFilters = new HashMap<>();
        andFilters.put("clauses.status", "REGISTERED");
        andFilters.put("common.citizen.address", "jakkasandra");

        SearchResult searchResult = searchService.search(asList(indexName), asList(), Filters.withAndFilters(andFilters), Sort.NULL, Page.NULL);
        assertThat(searchResult.documentCount(), is(2));
        assertThat(complaintNumbers(searchResult), containsInAnyOrder("810FBE", "892JBP"));
    }

    @Test
    public void shouldSearchWithAndPlusOrFilters() {
        Map<String, String> andFilters = new HashMap<>();
        andFilters.put("clauses.status", "REGISTERED");

        Map<String, String> orFilters = new HashMap<>();
        orFilters.put("searchable.title", "mosquito OR garbage");

        SearchResult searchResult = searchService.search(asList(indexName), asList(), withAndPlusOrFilters(andFilters, orFilters), Sort.NULL, Page.NULL);
        assertThat(searchResult.documentCount(), is(3));
        assertThat(complaintNumbers(searchResult), containsInAnyOrder("810FBE","820LGN", "751HFP"));
    }

    @Test
    public void shouldSearchWithOrFiltersOnDifferentFields() {
        Map<String, String> orFilters = new HashMap<>();
        orFilters.put("clauses.status", "COMPLETED");
        orFilters.put("clauses.mode", "INTERNET");

        SearchResult searchResult = searchService.search(asList(indexName), asList(), Filters.withOrFilters(orFilters), Sort.NULL, Page.NULL);
        assertThat(searchResult.documentCount(), is(4));
        assertThat(complaintNumbers(searchResult), containsInAnyOrder("299DIF", "751HFP", "696IDN", "873GBH"));
    }

    @Test
    public void shouldSearchWithOrFiltersOnSameField() {
        Map<String, String> andFilters = new HashMap<>();
        andFilters.put("clauses.status", "FORWARDED OR COMPLETED");

        SearchResult searchResult = searchService.search(asList(indexName), asList(), withAndFilters(andFilters), Sort.NULL, Page.NULL);
        assertThat(searchResult.documentCount(), is(2));
        assertThat(complaintNumbers(searchResult), containsInAnyOrder("299DIF","873GBH"));
    }

    @Test
    public void shouldSearchWithAndPlusNotInFilter() {
        Map<String, String> andFilters = new HashMap<>();
        andFilters.put("clauses.status", "REGISTERED");

        Map<String, String> notInFilters = new HashMap<>();
        notInFilters.put("clauses.mode", "CITIZEN");

        SearchResult searchResult = searchService.search(asList(indexName), asList(), withAndPlusNotFilters(andFilters, notInFilters), Sort.NULL, Page.NULL);
        assertThat(searchResult.documentCount(), is(2));
        assertThat(complaintNumbers(searchResult), containsInAnyOrder("751HFP","696IDN"));
    }
    @Test
    public void shouldSearchWithMultipleOrAndNotInFilter(){
    	 Map<String, String> orFilters = new HashMap<>();
    	 orFilters.put("clauses.status", "COMPLETED");
    	 orFilters.put("clauses.mode", "INTERNET");
    	 
    	 Map<String, String> notInFilters = new HashMap<>();
    	 notInFilters.put("searchable.title", "mosquito");
    	 
    	 SearchResult searchResult=searchService.search(asList(indexName), asList(indexType), Filters.withOrPlusNotFilters(orFilters,notInFilters), Sort.NULL, Page.NULL);
    	 assertThat(searchResult.documentCount(), is(2));
    	 assertThat(complaintNumbers(searchResult), containsInAnyOrder("873GBH","696IDN"));
    }

}
