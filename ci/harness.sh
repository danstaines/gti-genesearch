#!/bin/bash

export PERL5LIB=$PWD/bioperl-live:$PWD/ensembl-test/modules:$PWD/ensembl-orm/modules:$PWD/ensembl-taxonomy/modules:$PWD/ensembl-production/modules:$PWD/ensembl/modules:$PWD/search/src/main/perl/lib/:$PWD/search/src/test/perl/t/:$PWD/ensembl-hive/modules
export TEST_AUTHOR=$USER
export ENSEMBL_CVS_ROOT_DIR=$PWD
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$PWD/htslib

export PATH=$PATH:$PWD/htslib


echo "Running test suite"
echo "Using $PERL5LIB"
if [ "$COVERALLS" = 'true' ]; then
  PERL5OPT='-MDevel::Cover=+ignore,bioperl,+ignore,ensembl-test' perl $PWD/ensembl-test/scripts/runtests.pl -verbose $PWD/search/src/test/perl/t/ $SKIP_TESTS
else
  perl $PWD/ensembl-test/scripts/runtests.pl $PWD/search/src/test/perl/t/ $SKIP_TESTS
fi

rt=$?
if [ $rt -eq 0 ]; then
  if [ "$COVERALLS" = 'true' ]; then
    echo "Running Devel::Cover coveralls report"
    cover --nosummary -report coveralls
  fi
  exit $?
else
  exit $rt
fi