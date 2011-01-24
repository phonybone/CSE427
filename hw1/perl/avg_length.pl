#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i));
    Options::useDefaults(fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


sub main {
    my $ptt_file=shift or die "no .ptt file";
    open (PTT,$ptt_file) or die "Can't open $ptt_file: $!\n";
    my $n_diff++;
    my $orf_total=0;
    my $n_prots=0;
    my $gs=0;

    while (<PTT>) {
	chomp;
	if (/^(\d+) proteins/) {
	    $n_prots=$1;
	    warn "$n_prots proteins\n";
	} 
	elsif (/1\.\.(\d+)$/) {
	    $gs=$1;
	    warn "genome length: $gs\n";
	    next;
	}

	my ($orf,$strand,$aa_len)=split(/\s+/);
	next unless $orf=~/^(\d+)\.\.(\d+)$/;
	my ($start,$stop)=($1,$2);

	my $orf_length=($aa_len+1)*3;
	if (($stop-$start+1) != ($orf_length)) {
	    warn "differening lengths??? \n";
	    $n_diff++;
	}
	$orf_total+=$orf_length;
    }
    close PTT;

    my $avg_len=$orf_total/$n_prots;
    printf "avg_length: %.2f (%d/%d)\n", $avg_len, $orf_total, $n_prots;
    
    my $coverage=$orf_total/$gs*100;
    printf "coverage: %4.2f%% (%d/%d)\n", $coverage,$orf_total,$gs;
}

main(@ARGV);

