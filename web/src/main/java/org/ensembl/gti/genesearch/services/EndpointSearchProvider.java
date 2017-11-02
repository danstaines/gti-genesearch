/*
 * Copyright [1999-2016] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.gti.genesearch.services;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.bson.Document;
import org.elasticsearch.client.Client;
import org.ensembl.genesearch.Search;
import org.ensembl.genesearch.clients.ClientBuilder;
import org.ensembl.genesearch.impl.DivisionAwareSequenceSearch;
import org.ensembl.genesearch.impl.ESSearch;
import org.ensembl.genesearch.impl.ESSearchFlatten;
import org.ensembl.genesearch.impl.EVAGenomeFinder;
import org.ensembl.genesearch.impl.EVAGenomeRestSearch;
import org.ensembl.genesearch.impl.EVAVariantRestSearch;
import org.ensembl.genesearch.impl.ExpressionSearch;
import org.ensembl.genesearch.impl.GeneSearch;
import org.ensembl.genesearch.impl.SearchRegistry;
import org.ensembl.genesearch.impl.SearchType;
import org.ensembl.genesearch.impl.SolrSearch;
import org.ensembl.genesearch.impl.TranscriptSearch;
import org.ensembl.genesearch.impl.VariantSearch;
import org.ensembl.genesearch.info.DataTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;

/**
 * Utility class to allow construction and injection of searches for REST
 * endpoints
 * 
 * @author dstaines
 *
 */
@Component
public class EndpointSearchProvider {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    protected Search variantSearch = null;
    protected Search geneSearch = null;
    protected Search genomeSearch = null;
    protected Search transcriptSearch = null;
    protected Search expressionSearch = null;
    protected Search expressionAnalyticsSearch = null;
    protected Search sequenceSearch = null;
    protected Client client = null;
    protected MongoCollection<Document> mongoCollection = null;
    private SolrClient solrAnalyticsClient = null;
    private SolrClient solrExperimentsClient = null;
    private SearchRegistry registry = null;
    @Value("${es.host}")
    private String hostName;
    @Value("${es.cluster}")
    private String clusterName;
    @Value("${es.port}")
    private int port;
    @Value("${es.node}")
    private boolean node;
    @Value("${es.index}")
    private String index = ESSearch.GENES_INDEX;
    @Value("${rest.url.ens}")
    private String ensRestUrl;
    @Value("${rest.url.eg}")
    private String egRestUrl;
    @Value("${mongo.url}")
    private String mongoUrl;
    @Value("${mongo.database}")
    private String mongoDatabaseName;
    @Value("${mongo.collection}")
    private String mongoCollectionName;
    @Value("${solr.expression.url}")
    private String solrAnalyticsUrl;
    @Value("${solr.experiments.url}")
    private String solrExperimentsUrl;
    @Value("${eva.rest.url}")
    private String evaRestUrl;

    public EndpointSearchProvider() {
    }

    public Client getESClient() {
        if (client == null) {
            if (node) {
                log.info("Joining ES cluster " + this.clusterName + " via " + this.hostName);
                client = ClientBuilder.buildClusterClient(this.clusterName, this.hostName);
            } else {
                log.info("Connecting to ES cluster " + this.clusterName + " on " + this.hostName + ":" + this.port);
                client = ClientBuilder.buildTransportClient(this.clusterName, this.hostName, this.port);
            }
        }
        return client;
    }

    public void setESClient(Client client) {
        this.client = client;
    }

    public MongoCollection<Document> getMongoCollection() {
        if (mongoCollection == null) {
            log.info("Connecting to MongoDB " + mongoUrl);
            MongoClient mongoC = new MongoClient(new MongoClientURI(mongoUrl));
            log.info("Connecting to MongoDB " + mongoDatabaseName + "/" + mongoCollectionName);
            mongoCollection = mongoC.getDatabase(mongoDatabaseName).getCollection(mongoCollectionName);
        }
        return mongoCollection;
    }

    public void setMongoCollection(MongoCollection<Document> mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

    public SolrClient getSolrAnalyticsClient() {
        if (solrAnalyticsClient == null) {
            solrAnalyticsClient = new HttpSolrClient.Builder().withBaseSolrUrl(solrAnalyticsUrl).build();
        }
        return solrAnalyticsClient;
    }

    public void getSolrAnalyticsClient(SolrClient solrClient) {
        this.solrAnalyticsClient = solrClient;
    }

    public SolrClient getSolrExperimentsClient() {
        if (solrExperimentsClient == null) {
            solrExperimentsClient = new HttpSolrClient.Builder().withBaseSolrUrl(solrExperimentsUrl).build();
        }
        return solrExperimentsClient;
    }

    public void getSolrExperimentsClient(SolrClient solrClient) {
        this.solrExperimentsClient = solrClient;
    }

    public SearchRegistry getRegistry() {
        if (registry == null) {
            DataTypeInfo geneType = DataTypeInfo.fromResource("/genes_datatype_info.json");
            DataTypeInfo genomeType = DataTypeInfo.fromResource("/genomes_datatype_info.json");
            DataTypeInfo transcriptType = DataTypeInfo.fromResource("/transcripts_datatype_info.json");
            DataTypeInfo seqType = DataTypeInfo.fromResource("/sequences_datatype_info.json");

            Search esGenomeSearch = new ESSearch(getESClient(), index, ESSearch.GENOME_ESTYPE, genomeType);
            Search esGeneSearch = new ESSearch(getESClient(), index, ESSearch.GENE_ESTYPE, geneType);
            Search seqSearch = new DivisionAwareSequenceSearch(esGenomeSearch, seqType, getEnsRestUrl(),
                    getEgRestUrl());
            Search esTranscriptSearch = new ESSearchFlatten(getESClient(), index, ESSearch.GENE_ESTYPE, "transcripts",
                    "genes", transcriptType);

            DataTypeInfo evaGenomeType = DataTypeInfo.fromResource("/evagenomes_datatype_info.json");
            DataTypeInfo variantType = DataTypeInfo.fromResource("/evavariants_datatype_info.json");
            Search evaGenomesSearch = new EVAGenomeRestSearch(evaRestUrl, evaGenomeType);
            Search variantSearch = new EVAVariantRestSearch(evaRestUrl, variantType,
                    new EVAGenomeFinder(evaGenomesSearch, esGenomeSearch));

            DataTypeInfo expressionType = DataTypeInfo.fromResource("/expression_datatype_info.json");
            DataTypeInfo expressionExperimentsType = DataTypeInfo
                    .fromResource("/expression_experiments_datatype_info.json");
            Search solrExpressionSearch = new SolrSearch(getSolrAnalyticsClient(), expressionType);
            Search solrExpressionExperimentsSearch = new SolrSearch(getSolrExperimentsClient(),
                    expressionExperimentsType);

            registry = new SearchRegistry().registerSearch(SearchType.GENES, esGeneSearch)
                    .registerSearch(SearchType.TRANSCRIPTS, esTranscriptSearch)
                    .registerSearch(SearchType.HOMOLOGUES, esGeneSearch)
                    .registerSearch(SearchType.GENOMES, esGenomeSearch).registerSearch(SearchType.SEQUENCES, seqSearch)
                    .registerSearch(SearchType.VARIANTS, variantSearch)
                    .registerSearch(SearchType.EXPRESSION_ANALYTICS, solrExpressionSearch)
                    .registerSearch(SearchType.EXPRESSION_EXPERIMENTS, solrExpressionExperimentsSearch);

            expressionSearch = new ExpressionSearch(registry);

            registry.registerSearch(SearchType.EXPRESSION, expressionSearch);

        }
        return registry;
    }

    public Search getGeneSearch() {
        if (geneSearch == null) {
            geneSearch = new GeneSearch(getRegistry());
        }
        return geneSearch;
    }

    public Search getGenomeSearch() {
        if (genomeSearch == null) {
            genomeSearch = getRegistry().getSearch(SearchType.GENOMES);
        }
        return genomeSearch;
    }

    public void setGeneSearch(Search search) {
        this.geneSearch = search;
    }

    public void setGenomeSearch(Search search) {
        this.genomeSearch = search;
    }

    public String getEnsRestUrl() {
        return ensRestUrl;
    }

    public void setEnsRestUrl(String ensRestUrl) {
        this.ensRestUrl = ensRestUrl;
    }

    public String getEgRestUrl() {
        return egRestUrl;
    }

    public void setEgRestUrl(String egRestUrl) {
        this.egRestUrl = egRestUrl;
    }

    public Search getTranscriptSearch() {
        if (transcriptSearch == null) {
            transcriptSearch = new TranscriptSearch(getRegistry());
        }
        return transcriptSearch;
    }

    public void setTranscriptSearch(Search transcriptSearch) {
        this.transcriptSearch = transcriptSearch;
    }

    public Search getVariantSearch() {
        if (variantSearch == null) {
            variantSearch = new VariantSearch(getRegistry());
        }
        return variantSearch;
    }

    public void setVariantSearch(Search variantSearch) {
        this.variantSearch = variantSearch;
    }

    public Search getExpressionSearch() {
        if (expressionSearch == null) {
            expressionSearch = new ExpressionSearch(getRegistry());
        }
        return expressionSearch;
    }

    public Search getSequenceSearch() {
        return getRegistry().getSearch(SearchType.SEQUENCES);
    }

}
