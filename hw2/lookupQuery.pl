#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/sandbox/perl/PhonyBone';
use lib '/home/victor/sandbox/perl';
use Options;
use PhonyBone::FileUtilities qw(slurpFile spitString);

BEGIN: {
  Options::use(qw(d q v h fuse=i));
    Options::useDefaults(fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}

use constant PROT=>0;
use constant SCORE=>1;
use vars qw($VAR1);

sub main {
    my $query_file=shift or die "no query_file";
    my $query=slurpFile($query_file);

    my $index_file=shift or die "no index";
    warn "reading $index_file...\n";
    my $index_str=slurpFile($index_file);
    eval $index_str;	# hashref: k=t3, v=hash of {$prot => $count}
    my $index=$VAR1;
    warn sprintf "%d keys in index\n", scalar keys %$index;

    my $i=0;
    my $l=length($query)-2;
    my %score;			# k=subject protein; v=score
    while ($i<$l) {
	my $t3=substr($query,$i,$i+2);
	my $prots=$index->{$t3};
	if ($prots) {
	    while (my ($prot_id,$score)=each %$prots) {
		$score{$prot_id}+=$score;
	    }
	}
	$i++;
    }
    warn "Score: ",Dumper(\%score);

    # get list of prots sorted by score:
    # good ole schwartian transformation:
    my @s_prots=map {$_->[0]} sort {$b->[1] <=> $a->[1]} map {[$_,$score{$_}]} keys %score;
    foreach my $prot_id (@s_prots) {
	my $score=$score{$prot_id} || 0;
	print "$prot_id: $score\n";
    }
}




sub read_query {
    my $query_file=shift;
    my $query;
    open(QUERY,$query_file) or die "Can't open $query_file: $!\n";
    while (<QUERY>) { chomp; $query.=$_; }
    close QUERY;
    return $query;
}

main(@ARGV);

