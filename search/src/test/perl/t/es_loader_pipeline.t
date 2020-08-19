# Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
# Copyright [2016-2019] EMBL-European Bioinformatics Institute
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

use strict;
use warnings;

use Test::More;
use Bio::EnsEMBL::Test::RunPipeline;
use FindBin qw( $Bin );
use Search::Elasticsearch;

my $es_url = "http://elasticsearch:9200";
my $options= "-dumps_dir $Bin";
$options .= " -es_url $es_url";

ok(1, 'Startup test');

my $module = 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFiles_conf';
my $pipeline = Bio::EnsEMBL::Test::RunPipeline->new($module, $options);
$pipeline->run();
my $e = Search::Elasticsearch->new(nodes => $es_url);
# Testing genomes
my $genome = $e->get(
    index   => 'genomes',
    type    => 'genome',
    id      => 'danio_rerio'
);
is($genome->{_source}->{name}, 'danio_rerio', "Expected genome name");
is($genome->{_source}->{division}, 'EnsemblVertebrates', "Expected division");
is($genome->{_source}->{assembly}->{name}, 'GRCz11', "Expected assembly name");
# Testing Genes
my $gene = $e->get(
    index   => 'genes',
    type    => 'gene',
    id      => 'EPlOSAG00000002326'
);
is($gene->{_source}->{id}, 'EPlOSAG00000002326', "Expected gene ID");
is($gene->{_source}->{genome}, 'oryza_sativa', "Expected Genome name");
is($gene->{_source}->{biotype}, 'ncRNA', "Expected biotype");
# Testing Variants
my $variant = $e->get(
    index   => 'variants',
    type    => 'variant',
    id      => 'rs3'
);
is($variant->{_source}->{id}, 'rs3', "Expected Variant ID");
is($variant->{_source}->{class}, 'SNV', "Expected Class");
is($variant->{_source}->{ancestral_allele}, 'C', "Expected ancestral allele");
# Testing external_features
my $external_feature = $e->get(
    index   => 'external_features',
    type    => 'external_feature',
    id      => 'p12@APOM,0.1654'
);
is($external_feature->{_source}->{id}, 'p12@APOM,0.1654', "Expected External Features ID");
is($external_feature->{_source}->{set_name}, 'FANTOM', "Expected Genome name");
is($external_feature->{_source}->{genome}, 'homo_sapiens', "Expected Genome");
# Testing mirnas
my $mirna = $e->get(
    index   => 'mirnas',
    type    => 'mirna',
    id      => 'hsa-miR-4524a-5p'
);
is($mirna->{_source}->{id}, 'hsa-miR-4524a-5p', "Expected miRNA ID");
is($mirna->{_source}->{set_name}, 'TarBase miRNA', "Expected set name");
is($mirna->{_source}->{genome}, 'homo_sapiens', "Expected genome");
# Testing Motifs
my $motif = $e->get(
    index   => 'motifs',
    type    => 'motif',
    id      => 'ENSM00206643801'
);
is($motif->{_source}->{id}, 'ENSM00206643801', "Expected Motif ID");
is($motif->{_source}->{genome}, 'homo_sapiens', "Expected Genome name");
is($motif->{_source}->{stable_id}, 'ENSPFM0186', "Expected Stable ID");
# Testing Peaks
my $peak = $e->get(
    index   => 'peaks',
    type    => 'peak',
    id      => 'H3K4me3'
);
is($peak->{_source}->{id}, 'H3K4me3', "Expected Peak ID");
is($peak->{_source}->{genome}, 'homo_sapiens', "Expected Genome name");
is($peak->{_source}->{epigenome_name}, 'IHECRE00001049', "Expected Epigenome name");
# Testing Probes
my $probe = $e->get(
    index   => 'probes',
    type    => 'probe',
    id      => '74882'
);
is($probe->{_source}->{id}, '74882', "Expected Probe ID");
is($probe->{_source}->{genome}, 'homo_sapiens', "Expected Genome name");
is($probe->{_source}->{name}, 'GE790890', "Expected Probe name");
# Testing Probesets
my $probeset = $e->get(
    index   => 'probesets',
    type    => 'probeset',
    id      => 'homo_sapiens_probeset_1658'
);
is($probeset->{_source}->{id}, 'homo_sapiens_probeset_1658', "Expected Probeset ID");
is($probeset->{_source}->{genome}, 'homo_sapiens', "Expected Genome name");
is($probeset->{_source}->{name}, '1526_i_at', "Expected Name");
# Testing Regulatory features
my $regulatory_feature = $e->get(
    index   => 'regulatory_features',
    type    => 'regulatory_feature',
    id      => 'ENSR00000960956'
);
is($regulatory_feature->{_source}->{id}, 'ENSR00000960956', "Expected Regulatory ID");
is($regulatory_feature->{_source}->{genome}, 'homo_sapiens', "Expected Genome name");
is($regulatory_feature->{_source}->{so_name}, 'open_chromatin_region', "Expected SO name");
# Testing Transcription Factors
my $transcription_factor = $e->get(
    index   => 'transcription_factors',
    type    => 'transcription_factor',
    id      => 'VDR'
);
is($transcription_factor->{_source}->{id}, 'VDR', "Expected Transcription Factor ID");
is($transcription_factor->{_source}->{genome}, 'homo_sapiens', "Expected Genome name");
is($transcription_factor->{_source}->{type}, 'Transcription Factor', "Expected Type");

done_testing();
