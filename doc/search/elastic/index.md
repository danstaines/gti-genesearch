# Overview

The current implementation uses the search engine [Elasticsearch](https://www.elastic.co/products/elasticsearch) which supports indexing and querying of nested documents. Elasticsearch scales horizontally, so multiple nodes can be added to support increasing volumes of data.

This project provides pipelines for indexing JSON-formatted documents representing genes and genomes, and a simplified API to allow querying of indexed genes and genomes. Note that this does not support variation or sequence which are inappropriate for the implementation described.

# Installation

Creation of a search index requires two main steps:
- installation of [Elasticsearch](https://www.elastic.co/products/elasticsearch) on a cluster of machines
- creation and indexing by Elasticsearch of JSON formatted documents for each gene

## Elasticsearch

The current test infrastructure uses 8 data nodes (2CPU, 200Gb /data partition, 32Gb RAM) and 1 client node (2CPU, 100Gb /data partition, 4Gb RAM). The data nodes contain the documents and indices for Elasticsearch, and the client node provides a single contact point for client activities but does not contain any data. The rest of this document assumes the set up described.

To install Elasticsearch on the /data partition, download the latest 2.3 tarball from https://www.elastic.co/downloads/elasticsearch and untar under `/data`. Other versions should work but have not been tested.

Once installed, [`elasticsearch.yml`](src/main/resources/elasticsearch.yml) should be copied to `/data/elasticsearh/config`.

Some notes on this config file:
- unicast is used in preference to multicast - this is much more reliable on some clusters
- the size of the queue for bulk indexing is increased to support increased levels of concurrent indexing
- the node name is set to the host name (rather than the default which is a random Marvel character)
- data and log directories are kept separate for convenience
- mininum_master_nodes is set to 5 for a 9 node cluster (see notes online about the problem of "split brain"
- the last section should be uncommented on the head node:
```
node.data: false
node.master: false
```

Next, the plugin `elasticsearch-head` should be installed on the client "head" node - this provides some useful admin and development interfaces:
```
cd /data/elasticsearch
./bin/plugin install mobz/elasticsearch-head
```

Elasticsearch typically requires significant amounts of RAM. General advice is to give half the available RAM to the JVM heap with the remainder used for memory mapping indices. This requires that the environment variable `ES_HEAP_SIZE` is set to `16g`. Elasticsearch can then be started on each node in turn:
```
cd /data
export ES_HEAP_SIZE=16g
./elasticsearch/bin/elasticsearch
```
(this may be best done using a screen or tmux session)

Once completed, the main elasticsearch interface can be reached at http://gti-es-0:9200/ and the head plugin can be reached at http://gti-es-0:9200/_plugin/head

The next step is to set up the indices for genes and genomes on the cluster using [`setup_es.sh`](src/main/bin/setup_es.sh). This accepts a URL and a count for the number of shards. Currently 64 shards are recommended for the current set e.g.
```
cd ./src/main/bin
URL=http://localhost:9200/genes_84
N=64
./setup_es.sh $URL $IDX 
```
A recommended approach is to use a version-specific name for a given index (e.g. genes_84) and then add an alias of "genes" to that index for subsequent use:
```
curl -XPOST http://localhost:9200/_aliases -d '{"actions":[{"add":{"index":"genes_84","alias":"genes"}}]}'
```

## Gene indexing

JSON dumping is carried out using the [JSON dump pipeline](https://github.com/Ensembl/ensembl-production/blob/master/modules/Bio/EnsEMBL/Production/Pipeline/PipeConfig/DumpGenomeJson_conf.pm). 

This requires a registry file pointing to servers containing the genomes of interest e.g. the public MySQL servers:
```
{
  package reg;
  use strict;
  use warnings;
  use Bio::EnsEMBL::Registry;
  Bio::EnsEMBL::Registry->load_registry_from_multiple_dbs( {
                                -host => 'mysql-eg-publicsql.ebi.ac.uk',
                                -port => 4157,
                                -user => 'anonymous',
                                -db_version => 84}, {
                                -host       => 'ensembldb.ensembl.org',
                                -port       => 5306,
                                -user       => 'anonymous',
                                -db_version => 84} );
  # manually add vertebrate compara
  my $compara_dba =
    new Bio::EnsEMBL::Compara::DBSQL::DBAdaptor(
                                       -host => 'ensembldb.ensembl.org',
                                       -user => 'anonymous',
                                       -port => 5306,
                                       -species => 'ensembl',
                                       -dbname  => 'ensembl_compara_84'
    );
  1;
}
```
To run the pipeline from ensembl-production:
```
init_pipeline.pl EGExt::FTP::JSON::DumpGenomeJson_conf -registry $REG -pipeline_db -host=$HOST -port=$PORT -pipeline_db -user=$USER -pipeline_db -pass=$PASS -hive_force_init 1 -dumps_dir $DUMPS_DIR -run_all 1
# run beekeeper as instructed
```

One the dumps have completed, they can be indexed using the pipeline defined in [IndexJsonFiles_conf.pm](src/main/perl/lib/Bio/EnsEMBL/GTI/GeneSearch/Pipeline/IndexJsonFiles_conf.pm). This uses the Elasticsearch Perl API for bulk indexing, as implemented in [JsonIndexer.pm](src/main/perl/lib/Bio/EnsEMBL/GTI/GeneSearch/JsonIndexer.pm)

To run the pipeline:
```
init_pipeline.pl Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFiles_conf -pipeline_db -host=$HOST -port=$PORT -pipeline_db -user=$USER -pipeline_db -pass=$PASS -hive_force_init 1 -dumps_dir $DUMPS_DIR -es_url $ES_URL -blacklist $BLACKLIST -index $INX
# run beekeeper as instructed
```
`-blacklist` allows a list of genomes to skip during indexing to be specified - these might include redundant bacterial genomes. `-es_url` is the base URL of the elasticsearch head node, and `-index` allows the name of the index to be specified.

Once the indexing has completed, variation data can then be added. This is kept separate for reasons of efficiency, allowing parallelisation on a per-gene level rather than per genome. Variants are retrieved from the Ensembl variation databases and phenotype/consequence data written direct to gene documents in Elasticsearch. Note that individual variants are not added to genes due to the huge volume of data for some transcripts in human and cow (e.g. >150k variants per transcript).

The pipeline is defined in [AddVariation_conf.pm](src/main/perl/lib/Bio/EnsEMBL/GTI/GeneSearch/Pipeline/AddVariation_conf.pm):
```
init_pipeline.pl Bio::EnsEMBL::GTI::GeneSearch::Pipeline::AddVariation_conf -registry $REG -pipeline_db -host=$HOST -port=$PORT -pipeline_db -user=$USER -pipeline_db -pass=$PASS -hive_force_init 1 -es_url $ES_URL -index $IDX -hive_force_init 1
```

# Copyright and Licensing
Copyright 1999-2016 EMBL-European Bioinformatics Institute

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.