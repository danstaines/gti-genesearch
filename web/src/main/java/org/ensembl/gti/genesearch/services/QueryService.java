package org.ensembl.gti.genesearch.services;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ensembl.genesearch.GeneSearch;
import org.ensembl.genesearch.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Path("/query")
public class QueryService {

	final Logger log = LoggerFactory.getLogger(QueryService.class);
	protected final GeneSearch search;

	@Autowired
	public QueryService(GeneSearchProvider provider) {
		this.search = provider.getGeneSearch();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public QueryResult get(@BeanParam QueryParams params) {
		log.info("Get from query");
		return query(params);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public QueryResult post(QueryParams params) {
		log.info("Post to query");
		return query(params);
	}

	public QueryResult query(QueryParams params) {
		log.info("query:" + params);
		return search.query(params.getQueries(), params.getFields(),
				params.getFacets(), params.getLimit(), params.getSorts());
	}

}
