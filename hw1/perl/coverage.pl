#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
use File::Basename;

# Calculate the percentage of the genome residing in orfs.
# Also computes the average length of orfs.

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
    my $ptt_file=shift or die "no ptt file\n";
    open (PTT,$ptt_file) or die "Can't open $ptt_file: $!\n";
    my ($replicon,$path,$suffix)=fileparse($ptt_file,qr/\.ptt$/);

    my @list;
    my $sum_length=0;
    my $n_prots=0;
    while (<PTT>) {
	next unless /^(\d+)\.\.(\d+)\s+([-+])\s+(\d+)/;
	my ($start,$stop,$strand,$len)=($1,$2,$3,$4);
	my $last=$list[$#list];
	if ($last && $last->[1] > $stop) {
	    $last->[1]=$stop;
	} else {
	    push @list, [$start,$stop,$strand,$len];
	}

	$sum_length+=($stop-$start+1);
	$n_prots++;
    }
    close PTT;

    my $orf_bp=0;
    foreach my $c (@list) {
	$orf_bp+=($c->[1]-$c->[0]+1);
    }

    my $coverage=$orf_bp/$gs{$replicon}*100;
    printf "coverage: %g%% (%d/%d)\n",$coverage,$orf_bp,$gs{$replicon};

    my $ave_len=$sum_length/$n_prots;
    printf "average length: %.0f (%d/%d)\n", $ave_len,$sum_length,$n_prots;
}

main(@ARGV);

