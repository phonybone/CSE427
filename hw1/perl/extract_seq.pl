#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use Options;
use RaCodons;

BEGIN: {
  Options::use(qw(d q v h fuse=i ll:i replicon|r:s));
    Options::useDefaults(fuse => -1,
			 replicon=>'NC_011660',
			 ll=>60,
	);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


MAIN: {
    my ($start,$stop)=@ARGV;
    die "Missing start and/or stop\n" if (!$start && !$stop);

    my $filename=$options{replicon}.".fna.p";
    my $fc=RaCodons->new(filename=>$filename);
    my $seq=$fc->seq_at1($start,$stop);
    my $rseq=comp($seq);
    my $len=length($seq);

    my $ruler='----^----|';
    my $n_rulers=$options{ll}/length $ruler;

    my $i=0;
    my $ll=$options{ll};
    while ($i <= $len-$ll) {
	print substr($seq,$i,$ll),"\n";
	print $ruler x $n_rulers;
	printf " %d-%d\n",$i+$start,$i+$ll+$start-1;
	print substr($rseq,$i,$ll),"\n\n";
	$i+=$ll;
    }

    # print tail:
    unless ($len%$ll == 0) {
	print substr($seq,$i),"\n";
	print $ruler x $n_rulers;
	printf " %d-%d\n",$i+$start,$i+$ll+$start-1;
	print substr($rseq,$i),"\n";
    }

}

sub comp {
    my $seq=shift;
    $seq=~tr/ACGT/TGCA/;
    $seq=~tr/acgt/tgca/;
    return $seq;	# perl list/scalar context can be weird sometimes
}
