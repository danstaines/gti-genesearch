package org.ensembl.genesearch;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface GeneSearch {

	public class QuerySort {
		public static enum SortDirection {
			ASC, DESC
		};

		public final String field;
		public final SortDirection direction;

		public QuerySort(String field, SortDirection direction) {
			this.field = field;
			this.direction = direction;
		}
	}

	/**
	 * Retrieve all results matching the supplied queries
	 * 
	 * @param queries
	 * @param fieldNames
	 *            (if empty the whole document will be returned)
	 * @return
	 */
	public List<Map<String, Object>> query(Collection<GeneQuery> queries,
			String... fieldNames);

	/**
	 * Retrieve all results matching the supplied queries and process with the
	 * supplied consumer
	 * 
	 * @param consumer
	 * @param queries
	 * @param fieldNames
	 *            (if empty the whole document will be returned)
	 * @return
	 */
	public void query(Consumer<Map<String, Object>> consumer,
			Collection<GeneQuery> queries, String... fieldNames);

	/**
	 * Search with the supplied queries and return a summary object containing
	 * results and facets
	 * 
	 * @param queries
	 *            list of queries to combine with AND
	 * @param output
	 *            source fields to include
	 * @param facets
	 *            fields to facet over
	 * @param limit
	 *            number of hits to return
	 * @return
	 */
	public QueryResult query(Collection<GeneQuery> queries,
			List<String> output, List<String> facets, int limit,
			List<QuerySort> sorts);

}
