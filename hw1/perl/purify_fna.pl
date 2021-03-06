#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i));
    Options::useDefaults(fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


MAIN: {
    my $fna_file=shift;
    open (FNA,$fna_file) or die "Can't open $fna_file: $!\n";
    <FNA>;			# burn first line
    while (<FNA>) {
	chomp;
	print;
    }
    close FNA;
}
