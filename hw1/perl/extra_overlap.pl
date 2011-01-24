#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i replicon|r:s));
    Options::useDefaults(fuse => -1,
			 replicon=>'NC_011660',
	);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


sub main {
    my @suffixs=qw(TPs sTPs FPs FNs);
    my @list;
    my $replicon=$options{replicon};
    foreach my $s (@suffixs) {
	my $filename="$replicon.$s";
	open (FILE,$filename) or die "Can't open $filename: $!\n";
	while (<FILE>) {
	    chomp;
	    my @stuff=split(/\s+/);

	    $stuff[0]=~/(\d+)\.\.(\d+)/ or next;
	    my ($start,$stop)=($1,$2);
	    push @list,[$start,"$_\t$s\n"];
	}
	close FILE;
    }

    @list=sort {$a->[0] <=> $b->[0]} @list;

    open(OUTPUT,">$replicon.features") or die "Can't open $replicon.features: $!\n";
    foreach my $line (@list) {
	print OUTPUT $line->[1];
    }
    close OUTPUT;
    warn "$replicon.features written\n";
}

main(@ARGV);

