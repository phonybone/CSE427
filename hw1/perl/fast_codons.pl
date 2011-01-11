#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;
use Sys::Mmap;

BEGIN: {
  Options::use(qw(d q v h fuse=i));
    Options::useDefaults(fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


MAIN: {
    my $fna_file=shift;
    my @stats=stat $fna_file or die "Can't stat $fna_file: $!\n";
    my $genome_size=$stats[7];
    warn "genome_size: $genome_size\n";

    open(FNA_FILE,$fna_file) or die "Can't open $fna_file: $!\n";

    my $genome;
    my $offset=0;
    my $size=0;
    my $flags=MAP_SHARED;
    my $prots=PROT_READ;
    mmap($genome, $size, $prots, $flags, FNA_FILE, $offset) or die "mmap: $!\n";
    warn substr($genome,0,20), "\n";
    munmap($genome);
    close FNA_FILE;
    
}
