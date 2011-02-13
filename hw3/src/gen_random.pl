#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;

# write a sequence of random numbers to a file


BEGIN: {
  Options::use(qw(d q v h fuse=i));
    Options::useDefaults(fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


sub main {
    my $usage="$0: <output_file> <n> [max]\n";
    my $output_file=shift or die $usage;
    my $n=shift or die $usage;
    my $max=shift || 100;

    open (OUTPUT,">$output_file") or die "Can't open $output_file for writing: $!\n";
    while ($n--) {
	print OUTPUT rand($max), "\n";
    }
    close OUTPUT;
    warn "$output_file written\n";
}

main(@ARGV);

