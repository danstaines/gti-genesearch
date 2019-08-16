/*
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ensembl.genesearch.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.ConnectTransportException;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Utility to create and load an in-memory Elastic test server. Note that this
 * is included in the main source folder to allow reuse in downstream projects
 * e.g. REST server.
 *
 * @author dstaines
 */
public class ESTestClient {

    private Client client;
    private String clusterName;
    static Logger log = LoggerFactory.getLogger(ESTestClient.class);
    private static ElasticsearchContainer container;

    public ESTestClient() throws RuntimeException {
        TransportAddress transportAddress;
        Settings settings;
        // System.setProperty("es.set.netty.runtime.available.processors", "false");
        String testProfile = System.getenv("TEST_PROFILE");

        if (testProfile == null || testProfile.equals("local")) {
            container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:6.8.1");
            container.start();
            clusterName = "docker-cluster";
            transportAddress = new TransportAddress(container.getTcpHost());

        } else if (testProfile.equals("integration")) {

            try {
                log.info("Try to connect to existing test ES");
                // look for a accessible Test ES server available locally
                String elasticHost = System.getenv("elastic-host");
                transportAddress = new TransportAddress(InetAddress.getByName(elasticHost != null ? elasticHost : "localhost"), 9300);
                clusterName = "test-genesearch";
            } catch (UnknownHostException | ConnectTransportException | NoNodeAvailableException e) {
                log.info("Elastic test server connection error " + e.getMessage());
                throw new RuntimeException("Unable to connect to integration server");
            }
        } else {
            throw new RuntimeException("Unknown configuration profile");
        }
        settings = Settings.builder().put("cluster.name", clusterName).build();
        client = new PreBuiltTransportClient(settings).addTransportAddress(transportAddress);

        ClusterHealthResponse healthResponse = client.admin().cluster().prepareHealth()
                .setTimeout(TimeValue.timeValueMinutes(1)).execute().actionGet();
        if (healthResponse.isTimedOut()) {
            throw new RuntimeException("ES Service Not available");
        }
        log.info(String.format("Connected to ES %s test server", testProfile));
    }

    /**
     * Read a mapping file and create an index. Resource is of the form
     * /{type}_index.json
     *
     * @param index name of index to create
     * @param type  mapping file type
     */
    public void createIndex(String index, String type) {
        try {
            log.info("Reading " + index + " mapping");
            // slurp the mapping file into memory
            String geneMapping = IOUtils.toString(ESTestClient.class.getResourceAsStream("/indexes/" + type + "_index.json"), Charset.defaultCharset());
            geneMapping = geneMapping.replaceAll("SHARDN", "1");
            geneMapping = geneMapping.replaceAll("REPLICAS", "0");
            Map<String, Object> geneIndexObj = mapper.readValue(geneMapping, new TypeReference<Map<String, Object>>() {
            });

            if (client.admin().indices().prepareExists(index).execute().actionGet().isExists()) {
                log.info("Index exists... Resetting");
                client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet();
            } else {
                log.info("Creating index");
            }
            // create an index with mapping
            Map<String, Object> mappingObj = (Map<String, Object>) geneIndexObj.get("mappings");
            client.admin().indices().prepareCreate(index).setSettings((Map<String, Object>) geneIndexObj.get("settings")).get();
            client.admin().indices().preparePutMapping(index).setType(type).setSource(mapper.writeValueAsString(mappingObj.get(type)), XContentType.JSON).get();
            log.info("Index created");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Client getClient() {
        return client;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Index the supplied JSON document into the specified index as the
     * specified type
     *
     * @param json
     * @param index
     * @param type
     */
    public void indexTestDocs(String json, String index, String type) {
        try {

            log.info("Indexing [Index:" + index + "][Type:" + type + "]");

            int n = 0;
            List<Map<String, Object>> docs = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });

            for (Map<String, Object> doc : docs) {
                indexTestDoc(doc, index, type);
                n++;
            }
            // wait for indices to be built
            client.admin().indices().refresh(new RefreshRequest(index)).actionGet();
            log.info("Indexed " + n + " documents");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Index the supplied object into the specified index as the specified type
     *
     * @param doc
     * @param index
     * @param type
     * @throws JsonProcessingException
     */
    protected void indexTestDoc(Map<String, Object> doc, String index, String type) throws
            JsonProcessingException {
        String id = String.valueOf(doc.get("id"));
        log.info("Id used " + id);
        client.prepareIndex(index, type, id).setSource(mapper.writeValueAsString(doc), XContentType.JSON).execute().actionGet();
    }

    protected boolean hasContainer() {
        return (container != null);
    }

    /**
     * Close the client and shut down the ES node.
     */
    public void disconnect() {
        client.close();
        if (container != null)
            container.close();
    }

}
