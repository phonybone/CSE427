#!/usr/bin/env perl 
#-*-perl-*-

use strict;
use warnings;
use Carp;
use Data::Dumper;
use Test::More qw(no_plan);
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;
use FastCodons;

BEGIN: {
  Options::use(qw(d q v h fuse=i min_condons:i));
    Options::useDefaults(fuse => -1,
			 min_codons => 125,
	);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


MAIN: {
    my $fc=FastCodons->new(filename=>'../../NC_011660.fna.p');
    my $fuse=$options{fuse};

    my $output_file=$ARGV[0];
    open (REPORT,$output_file) or die "Can't open $output_file: $!\n";
    while (my $line=<REPORT>) {
	next unless $line=~/^(\d+)\.\.(\d+)/;
	my ($start,$stop)=($1,$2);

	# print orf:
	my $orf=$fc->seq_at($start-1,$stop);
	print "$orf\n";
	is(length($orf)%3,0,"length ok");
	

	# test start codon
	my $start_codon=$fc->codon_at($start);
	ok(is_start($start_codon),"got start codon") or
	    print "got $start_codon\n";

	# test stop codon
	my $stop_codon=$fc->codon_at($stop-2); # $stop is last base of codon
	ok(is_stop($stop_codon),"got stop codon") or
	    print "got $stop_codon\n";

	# test all other codons
	$start+=3*$options{min_codons};
	while ($start<$stop-3) {
	    my $codon=$fc->codon_at($start);
	    ok(!is_stop($codon)) or 
		print "Found early stop codon at $start: ", $codon, " (stop=$stop)\n";
	    $start+=3;
	}

	last if --$fuse==0;
    }
    close REPORT;
}

sub is_start { $_[0] eq 'ATG' }
sub is_stop { $_[0]=~/^TAG|TAA|TGA$/ }
