=head1 LICENSE

.. See the NOTICE file distributed with this work for additional information
   regarding copyright ownership.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

=cut

package Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFiles_conf;

use strict;
use warnings;

use base ('Bio::EnsEMBL::Hive::PipeConfig::HiveGeneric_conf');

sub resource_classes {
    my ($self) = @_;
    return {
        'default' => { 'LSF' => '-q production-rh74' }
    };
}

sub default_options {
    my ($self) = @_;
    return { %{$self->SUPER::default_options()},
        dumps_dir      => '.',
        blacklist      => undef,
        es_url         => 'http://127.0.0.1:9200/',
        genes_index    => 'genes',
        genomes_index  => 'genomes',
        variants_index => 'variants',
        probes_index => 'probes',
        probesets_index => 'probesets',
        motifs_index => 'motifs',
        regulatory_features_index => 'regulatory_features',
        external_features_index => 'external_features',
        mirnas_index => 'mirnas',
        peaks_index => 'peaks',
        transcription_factors_index => 'transcription_factors'
    };
}

sub pipeline_analyses {
    my ($self) = @_;
    return [
        {
            -logic_name  => 'JsonFileFactory',
            -module      => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::JsonFileFactory',
            -rc_name    => 'default',
            -input_ids   => [ { dumps_dir => $self->o('dumps_dir') } ],
            -parameters  => { blacklist => $self->o('blacklist') },
            -flow_into   => {
                '2' => [ 'IndexGenomeJsonFile' ],
                '3' => [ 'IndexGenesJsonFile' ],
                '4' => [ 'IndexVariantsJsonFile' ],
                '5' => [ 'IndexProbesJsonFile' ],
                '6' => [ 'IndexProbesetsJsonFile' ],
                '7' => [ 'IndexExternalFeaturesJsonFile' ],
                '8' => [ 'IndexMirnaJsonFile' ],
                '9' => [ 'IndexPeaksJsonFile' ],
                '10' => [ 'IndexMotifsJsonFile' ],
                '11' => [ 'IndexRegulatoryFeaturesJsonFile' ],
                '12' => [ 'IndexTranscriptionFactorsJsonFile' ],
            }
        },
        {
            -logic_name    => 'IndexGenomeJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('genomes_index'),
                'type'     => 'genome',
                'id'       => 'id',
                'is_array' => 0
            }
        },
        {
            -logic_name    => 'IndexGenesJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('genes_index'),
                'type'     => 'gene',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexVariantsJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('variants_index'),
                'type'     => 'variant',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexProbesJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('probes_index'),
                'type'     => 'probe',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexProbesetsJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('probesets_index'),
                'type'     => 'probeset',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexExternalFeaturesJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('external_features_index'),
                'type'     => 'external_feature',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexMirnaJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('mirnas_index'),
                'type'     => 'mirna',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexPeaksJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('peaks_index'),
                'type'     => 'peak',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexMotifsJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('motifs_index'),
                'type'     => 'motif',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexRegulatoryFeaturesJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('regulatory_features_index'),
                'type'     => 'regulatory_feature',
                'id'       => 'id',
                'is_array' => 1
            }
        },
        {
            -logic_name    => 'IndexTranscriptionFactorsJsonFile',
            -module        => 'Bio::EnsEMBL::GTI::GeneSearch::Pipeline::IndexJsonFile',
            -hive_capacity => 10,
            -rc_name       => 'default',
            -parameters    => {
                'es_url'   => $self->o('es_url'),
                'index'    => $self->o('transcription_factors_index'),
                'type'     => 'transcription_factor',
                'id'       => 'id',
                'is_array' => 1
            }
        }
    ];
} ## end sub pipeline_analyses

1;
