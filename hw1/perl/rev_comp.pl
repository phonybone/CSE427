#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i no_header));
    Options::useDefaults(fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


MAIN: {
    if (my $filename=$ARGV[0]) {
	open(INPUT,$filename) or die "Can't open $filename: $!\n";
    } else {
	*INPUT=*STDIN;
    }

    my $header=<INPUT> unless $options{no_header};

    my @r;
    while (<INPUT>) {
	chomp;
	push @r, complement($_);
    }
    close(INPUT) if $ARGV[1];

    print $header unless $options{no_header};
    foreach my $r (reverse (@r)) {
	print "$r\n";
    }
}

sub complement {
    my $seq=shift;
    $seq=~tr/ACGT/TGCA/;
    $seq=~tr/acgt/tgca/;
    return scalar reverse $seq;	# perl list/scalar context can be weird sometimes
}
