#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i min:f max:f nbins:f));
    Options::useDefaults(fuse => -1, nbins=>20);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


sub main {
    # read file
    my $filename=shift or die usage(qw(<filename>));
    open (FILE,$filename) or die "Can't open $filename: $!\n";
    my $nbin;
    my ($min,$max,$nbins)=@options{qw(min max nbins)};
    my ($max_seen, $min_seen)=(-2**32,2**32);
    my @histo;
    while (<FILE>) {
	chomp;
	do { warn "$_\n"; next } unless (/^-?(?:\d+(?:\.\d*)?|\.\d+)$/); # is it a double? see perlfaq4
	if ($_<$min) { $nbin=0 }
	elsif ($_>$max) {$nbin=$nbins+2}
	else { $nbin=int(($_-$min)/($max-$min) * $nbins) }

	warn "$_ going to bin $nbin\n" if $ENV{DEBUG};
	$histo[$nbin]++;
	
	$min_seen=$_ if ($_<$min_seen);
	$max_seen=$_ if ($_>$max_seen);
    }
    close FILE;

    # print out histo, min, max
    print "min value: $min_seen\n";
    print "max value: $max_seen\n";
    for (my $i=0; $i<=$nbins; $i++) {
	my $val=defined $histo[$i]? $histo[$i] : 0;
	print "$i: $val\n";
    }
}

main(@ARGV);

