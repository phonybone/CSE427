#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i results:s predictions:s));
    Options::useDefaults(results=>'results',
			 predictions=>'../../NC_011660.ptt',
			 fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


sub stats {
    my %totals;
    foreach my $strand ('+','-') {
	my ($start2codon, $stop2codon)=load_predictions($strand);
	$totals{B}+=scalar keys %$start2codon;
	compare_predictions($strand,$start2codon,$stop2codon,\%totals);
    }

    $totals{Sn}=$totals{nTP}/$totals{ncbi};
    $totals{sSn}=($totals{nTP}+$totals{nsTP})/$totals{ncbi};
    $totals{FOR}=$totals{nFN}/$totals{ncbi};
    $totals{PPV}=$totals{nTP}/$totals{B};
    $totals{sPPV}=($totals{nTP}+$totals{nsTP})/$totals{B};
    $totals{FDR}=$totals{nFP}/$totals{B};
    print Dumper(\%totals);
}

sub compare_predictions {
    my ($strand,$start2codon,$stop2codon,$totals)=@_;
    my (@TPs,@sTPs,@FNs);
    my $ncbi_codons_file=$options{predictions} or die "no ncbi codons file\n";
    open (NCBI,$ncbi_codons_file) or die "Can't open $ncbi_codons_file: $!\n";
    while (<NCBI>) {
	chomp;
	my ($range,$pstrand,$junk)=split(/\s+/);
	next unless $pstrand eq $strand;
	next unless $range=~/^(\d+)..(\d+)$/;
	$totals->{ncbi}+=1;
	my ($start,$stop)=($1,$2);
	my $codon=[$start,$stop,$pstrand];

	if ($start2codon->{$start} && $stop2codon->{$stop}) {
	    push @TPs, $codon;
	    delete $start2codon->{$start};
	    delete $stop2codon->{$stop};
	} elsif ($stop2codon->{$stop}) {
	    push @sTPs, $codon;
	    delete $stop2codon->{$stop};
	} else {
	    push @FNs, $codon;
	}
    }
    close NCBI;

    my @FPs=map {[@$_,$strand]} values %$start2codon;
    my $nTP=scalar @TPs;
    my $nsTP=scalar @sTPs;
    my $nFP=scalar @FPs;
    my $nFN=scalar @FNs;

    print "\nstrand: $strand\n";
    printf "%d TPs\n",  $nTP;
    printf "%d sTPs\n", $nsTP;
    printf "%d FPs\n",  $nFP;
    printf "%d FNs\n",  $nFN;


    write_results("True Positives","TPs",$strand,\@TPs);
    write_results("semi-True Positives","sTPs",$strand,\@sTPs);
    write_results("False Positives","FPs",$strand,\@FPs);
    write_results("False Negatives","FNs",$strand,\@FNs);

    $totals->{nTP}+=$nTP;
    $totals->{nsTP}+=$nsTP;
    $totals->{nFP}+=$nFP;
    $totals->{nFN}+=$nFN;
}

sub write_results {
    my ($title, $short_title, $strand, $list)=@_;
    my $filename="$options{results}.$strand.${short_title}";
    open (RESULTS,">$filename") or die "Can't open $filename for writing: $!\n";
    print RESULTS "# $title\n";
    foreach my $codon (@$list) {
	if (@$codon != 3) {
	    die "bad codon?\n",Dumper($codon);
	}
	printf RESULTS "%d..%d\t%s\t%d\n",@$codon, ($codon->[1]-$codon->[0]+1);
    }
    close RESULTS;
    warn "$filename written\n";
}

sub hashify_predictions {
    my $predictions=shift or die "no predictions";
    my $strand=shift or die "no strand";

    my $start2codon={};
    my $stop2codon={};
    foreach my $codon (@$predictions) {
	my ($start,$stop,$strand)=@$codon;
	my $codon=[$start,$stop];
	$start2codon->{$start}=$codon;
	$stop2codon->{$stop}=$codon;
    }
    close FILE;
    ($start2codon,$stop2codon);
}
