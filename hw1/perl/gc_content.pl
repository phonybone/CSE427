#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
use File::Basename;

# Calculate the percentage of the genome residing in orfs.

use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i));
    Options::useDefaults(fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}

my %gs=(NC_011660=>2976212,
	NC_007618=>2121359,
	NC_007624=>1156948,
    );

sub main {
    my $fna_file=shift or die "no fna file\n";
    open (FNA,$fna_file) or die "Can't open $fna_file: $!\n";
    my ($replicon,$path,$suffix)=fileparse($fna_file,qr/\.fna$/);
    my $burn=<FNA>;

    my $gc=0;
    while (<FNA>) {
	chomp;
	foreach my $b (split(//)) {
	    $gc++ if $b eq 'C' || $b eq 'G';
	}
    }
    close FNA;

    my $coverage=$gc/$gs{$replicon}*100;
    printf "coverage: %g%% (%d/%d)\n",$coverage,$gc,$gs{$replicon};
    
}

main(@ARGV);

