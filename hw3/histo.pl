#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i min:f max:f nbins:f bar_char:s bar_scale:f));
    Options::useDefaults(fuse => -1, nbins=>20, bar_char=>'#', bar_scale=>1.0);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


sub main {
    # read file
    my $filename=shift or die usage(qw(<filename>));
    
    my $ncol=get_ncol($filename);
    histo_1col($filename) if ($ncol==1);
    histo_2col($filename) if ($ncol==2);
}

sub get_ncol {
    my $filename=shift or confess "no filename";
    my $ncol;
    open (FILE,$filename) or die "Can't open $filename: $!\n";
    while (<FILE>) {
	next if /^#/;
	my @fields=split(/\t/);
	$ncol=scalar @fields if (@fields<=2);
	last if defined $ncol;
    }
    die "Can't determine number of fields in $filename\n" unless defined $ncol;
    return $ncol;
}


sub histo_1col {
    my $filename=shift;
    open (FILE,$filename) or die "Can't open $filename: $!\n";
    my ($min,$max,$nbins)=@options{qw(min max nbins)};
    my ($max_seen, $min_seen)=(-2**32,2**32);
    my $max_count=0;		# the count for the most populated bin

    my @histo;
    while (my $d=<FILE>) {
	chomp $d;
	do { warn "$d\n"; next } unless (/^-?(?:\d+(?:\.\d*)?|\.\d+)$/); # is it a double? see perlfaq4
	my $nbin=get_bin($d,$min,$max,$nbins);

	warn "$d going to bin $nbin\n" if $ENV{DEBUG};
	if (++$histo[$nbin] > $max_count) {
	    $max_count=$histo[$nbin]; # keep track highest count
	}
	
	$min_seen=$d if ($d<$min_seen);
	$max_seen=$d if ($d>$max_seen);
    }
    close FILE;

    # print out histo, min, max
    print "min value: $min_seen\n" if $ENV{DEBUG};
    print "max value: $max_seen\n" if $ENV{DEBUG};
    my $span=($max-$min)/$nbins;
    for (my $i=0; $i<=$nbins; $i++) {
	my $val=defined $histo[$i]? $histo[$i] : 0;
	my $bar=$options{bar_char} x ($val/$options{bar_scale});
	printf "%4d: %8d $bar\n", $i*$span+$min, $val;
    }

}

sub get_bin {
    my ($d,$min,$max,$nbins)=@_;
    my $nbin;
    if ($d<$min)    { $nbin=0 }
    elsif ($d>$max) { $nbin=$nbins+2}
    else            { $nbin=int(($d-$min)/($max-$min) * $nbins) }
}

sub histo_2col {
    my ($filename)=@_;
    die "no filename\n" unless $filename;
    open (FILE,$filename) or die "Can't open $filename: $!\n";
    my %histo;

    while (<FILE>) {
	chomp;
	my @fields=split(/\t/);
	next unless @fields==2;
	$histo{$fields[0]}=$fields[1];
    }
    close FILE;
    
    foreach my $k (sort keys %histo) {
	my $bar=$options{bar_char} x ($histo{$k}/$options{bar_scale});
	printf "%10s: %4d %s\n", $k, $histo{$k}, $bar;
    }
}


main(@ARGV);

