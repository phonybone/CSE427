#!/bin/env perl
use strict;
use warnings;
use Carp;
use Data::Dumper;
use Options;

BEGIN: {
  Options::use(qw(d h));
    Options::useDefaults();
    Options::get();
    die usage() if $options{h};
    $ENV{DEBUG}=1 if $options{d};
}

MAIN: {
    my $blosum_file=shift or die "no blosum62 file\n";
    open (BLOSUM62,$blosum_file) or die "Can't open $blosum_file: $!\n";

    my %prots;
    my %prot2index;
    my %index2prot;
    my @prot_order;

    while (<BLOSUM62>) {
	chomp;
	next if /^#/;

	if (/^[\sA-Z*]+$/) {	# row of protein letters
	    @prot_order=grep /[A-Z*]/, split(/\s+/);
	    next;

	} else {
	    my ($prot,@values)=split(/\s+/);
	    foreach my $p (@prot_order) {
		$prots{$prot}->{$p}=shift @values;
	    }
	}

    }
    close BLOSUM62;

    # fill in missing:
    my $missing_value=-100;
    my @missing=qw(J O U);
    foreach my $p (@prot_order) {
	foreach my $m (@missing) {
	    $prots{$p}->{$m}=$missing_value;
	}
    }
    my $l=[(split(/ /,"$missing_value "x25))];
    foreach my $m (@missing) {
	foreach my $p (@prot_order,@missing) {
	    $prots{$m}->{$p}=$missing_value;
	}
    }
    
    # now print as java code:
    push @prot_order,@missing;
    my @index_order=sort @prot_order;
    my $matrix="private int[][] BLOSUM62=new int[][] {\n";

    foreach my $p1 (@index_order) {
	my $row="{";
	$row.=join(", ", map {sprintf "%4d",$prots{$p1}->{$_}} @index_order);
	$matrix.="$row},      // $p1\n";
    }
    $matrix.="};\n";
    print $matrix;
}
