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

package Bio::EnsEMBL::GTI::GeneSearch::Pipeline::JsonFileFactory;
use warnings;
use strict;
use base ('Bio::EnsEMBL::Production::Pipeline::Common::Base');

use File::Find;
use File::Slurp;
use Data::Dumper;

sub run {
    my $self = shift @_;
    my $base_dir = $self->param_required("dumps_dir");
    my ($genome_files,$genes_files,$variants_files,$probes_files,$probesets_files,$external_features_files,$mirna_files,$peaks_files,$motifs_files,$regulatory_features_files,$transcription_factors_files) = ([],[],[],[],[],[],[],[],[],[],[]);
    my $blacklist = {};
    if (defined $self->param('blacklist')) {
        $blacklist = { map {chomp;
            $_ => 1} read_file($self->param('blacklist')) };
    }
    $self->log->info("Processing files from $base_dir");
    find(
        sub {
            $self->log->debug("Processing " . $_);
            if (m/_genome\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_genome\.json$/$1/;
                $self->log->debug("genome file for $name");
                push @$genome_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_genes\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_genes\.json$/$1/;
                $self->log->debug("genes file for $name");
                push @$genes_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_variants\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_variants\.json$/$1/;
                $self->log->debug("variants file for $name");
                push @$variants_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_probes\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_probes\.json$/$1/;
                $self->log->debug("probes file for $name");
                push @$probes_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_probesets\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_probesets\.json$/$1/;
                $self->log->debug("probesets file for $name");
                push @$probesets_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_external_features\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_external_features\.json$/$1/;
                $self->log->debug("external_features file for $name");
                push @$external_features_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_mirna\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_mirna\.json$/$1/;
                $self->log->debug("mirna file for $name");
                push @$mirna_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_peaks\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_peaks\.json$/$1/;
                $self->log->debug("peaks file for $name");
                push @$peaks_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_motifs\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_motifs\.json$/$1/;
                $self->log->debug("motifs file for $name");
                push @$motifs_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_regulatory_features\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_regulatory_features\.json$/$1/;
                $self->log->debug("regulatory_features file for $name");
                push @$regulatory_features_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
            elsif (m/_transcription_factors\.json$/) {
                (my $name = $_) =~ s/([A-Za-z0-9_]+)_transcription_factors\.json$/$1/;
                $self->log->debug("transcription_factors file for $name");
                push @$transcription_factors_files, { file => $File::Find::name } unless $blacklist->{$name};
            }
        },
        $base_dir);
    $self->dataflow_output_id($genome_files, 2);
    $self->log->info(scalar(@$genome_files) . ' genome index jobs have been created');
    $self->dataflow_output_id($genes_files, 3);
    $self->log->info(scalar(@$genes_files) . ' genes index jobs have been created');
    $self->dataflow_output_id($variants_files, 4);
    $self->log->info(scalar(@$variants_files) . ' variants index jobs have been created');
    $self->dataflow_output_id($probes_files, 5);
    $self->log->info(scalar(@$probes_files) . ' probes index jobs have been created');
    $self->dataflow_output_id($probesets_files, 6);
    $self->log->info(scalar(@$probesets_files) . ' probesets index jobs have been created');
    $self->dataflow_output_id($external_features_files, 7);
    $self->log->info(scalar(@$external_features_files) . ' external_features index jobs have been created');
    $self->dataflow_output_id($mirna_files, 8);
    $self->log->info(scalar(@$mirna_files) . ' mirna index jobs have been created');
    $self->dataflow_output_id($peaks_files, 9);
    $self->log->info(scalar(@$peaks_files) . ' peaks index jobs have been created');
    $self->dataflow_output_id($motifs_files, 10);
    $self->log->info(scalar(@$motifs_files) . ' motifs index jobs have been created');
    $self->dataflow_output_id($regulatory_features_files, 11);
    $self->log->info(scalar(@$regulatory_features_files) . ' regulatory_features index jobs have been created');
    $self->dataflow_output_id($transcription_factors_files, 12);
    $self->log->info(scalar(@$transcription_factors_files) . ' transcription_factors index jobs have been created');
    return;
}

1;
