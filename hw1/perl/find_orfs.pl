#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
use File::Basename;
 
use Options;
use CodonStream;

use vars qw($usage $filename $replicon);

BEGIN: {
    Options::use(qw(d q v h min_codons:i fuse:i force no_save_orfs));
    Options::useDefaults(min_codons=>125,
			 fuse => -1,
	);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};

    my $script=basename($0);
    $usage="$script <filename> [options]\n";
}


sub main {
    # get filename
    $filename=shift or die $usage;
    my ($name,$path,$suffix)=fileparse($filename,qr/\..*/);
    my $replicon=$name;
    my %names=(NC_011660=>'Listeria monocytogenes HCC23',
	       NC_007618=>'Brucella melitensis biovar Abortus 2308',
    );
        
    my $orfs=[];

    # do '+' strand:
    my $strand_orfs=read_strand($filename,'+');
    push @$orfs,@$strand_orfs;

    # do '-' strand: have to generate rc first:
    my ($rc_filename,$gs)=write_rev_comp($filename);
    $strand_orfs=read_strand($rc_filename,'-',$gs);
    push @$orfs,@$strand_orfs;

    my $stats=stats($orfs);
    report("$replicon.txt",$orfs,$stats,$replicon,$names{$replicon});


    # sort @orfs on start index
    cleanup();
}

sub write_rev_comp {
    my $filename=shift or die "no filename";
    open (INPUT,$filename) or die "Can't open $filename: $!\n";
    open (OUTPUT,">$filename.rev") or die "Can't open $filename.rev for writing: $!\n";

    my $header=<INPUT> unless $options{no_header};
    chomp $header;
    my @r;
    my $gs=0;
    while (<INPUT>) {
	chomp;
	push @r, revcomp($_);
	$gs+=length $_;
    }
    close(INPUT);

    print OUTPUT "$header rev comp\n" unless $options{no_header};
    foreach my $r (reverse (@r)) {
	print OUTPUT "$r\n";
    }
    close OUTPUT;
    warn "$filename.rev written\n";
    return ("$filename.rev",$gs);
}

sub revcomp {
    my $seq=shift;
    $seq=~tr/ACGT/TGCA/;
    $seq=~tr/acgt/tgca/;
    return scalar reverse $seq;	# perl list/scalar context can be weird sometimes
}

sub cleanup {
    unlink "$filename.rev" if (-e "$filename.rev");
}

# returns $orfs
sub read_strand {
    # get input filename and figure out which codon server to use:
    my $fna_file=shift or die "no .fna file\n";
    my $strand=shift or die "no strand";
    my $gs=shift;
    die "no gs" if $strand eq '-' and not defined $gs;

    my $cs=CodonStream->new(filename=>$fna_file);
    my $orfs=find_orfs($cs,$strand,$gs);   
}

# returns a list[ref] of codom elements ([$start,$stop])
sub find_orfs {
    my ($cs,$strand,$gs,$use_saved_orfs)=@_;
    die "no gs" if $strand eq '-' and not defined $gs;
    my @orfs;			# results go here

    # check for previous results:
    my $orfs_file=$filename;
    $orfs_file=~s/\.fna/.orfs/;
    if ($use_saved_orfs && -e $orfs_file) {
	if (open(ORFS,$orfs_file)) {
	    while (<ORFS>) {
		my @orf=split(/\s+/);
		push @orfs,\@orf if ($orf[2] eq $strand);
	    }
	    close ORFS;
	    if (@orfs) {
		return wantarray? @orfs:\@orfs;
	    }
	}
    }

    my $frame=1;
    my @frame_start=(0,0,0,0);	# 1-based; first 0 a placeholder
    my $start_codon='ATG';
    my $stop_codons=[qw(TAG TAA TGA)];
    my $n=1;
    my $fuse=$options{fuse};

    while (my $codon=$cs->next) {
	my $frame=(($n-1)%3)+1;	# frames are 1-based
	warn "$n codons...\n" if $n%10000==0;

	if ($codon eq $start_codon && $frame_start[$frame]==0) {
	    $frame_start[$frame]=$n;
	    warn "staring orf ($codon): n=$n, frame=$frame\n" if $ENV{DEBUG};
	    
	} elsif (is_stop($codon) 
		 && $frame_start[$frame] != 0
		 && $n-$frame_start[$frame] >= $options{min_codons}*3) {

	    my $start=$frame_start[$frame];
	    my $stop=$n+2;

	    if ($strand eq '-') {
		my $temp=$start;
		$start=$gs-$stop+1;
		$stop=$gs-$temp+1;
	    }

	    push @orfs,[$start,$stop,$strand];
	    $frame_start[$frame]=0;

	    warn(sprintf "orf found ($codon): frame %d: %d-%d (l=%d)\n",
		 $frame,$start,$stop,(($n-$frame_start[$frame])%3))
		 if $ENV{DEBUG};
	}
	$n++;
	last if --$fuse==0;
    }

    # save orfs for next time:
    unless ($options{no_save_orfs}) {
	if (open(ORFS,">>$orfs_file")) {
	    foreach my $orf (@orfs) {
		print ORFS join("\t",@$orf),"\n";
	    }
	    close ORFS;
	    warn "$orfs_file written\n";
	}
    }

    return wantarray? @orfs:\@orfs;
}

sub dump_orfs {
    my ($orfs,$strand)=@_;
    my $filename="results.$strand";
    open (DUMP,">$filename") or die "Can't open $filename for writing: $!\n";
    foreach my $orf (sort {$a->[0] <=> $b->[0]} @$orfs) {
	print DUMP join("\t",@$orf),"\n";
    }
    close DUMP;
    warn "$filename written\n";
}

sub is_stop { $_[0]=~/^TAG|TAA|TGA$/ }

sub report {
    my ($output_filename,$orfs,$stats,$replicon,$pro_name)=@_;
    my @report_order=qw(TP sTP FN FP Sn sSn FOR PPV sPPV FDR);
    
    open (REPORT,">$output_filename") or die "Can't open $output_filename for writing: $!\n";

    print REPORT "$pro_name\n";
    foreach my $key (@report_order) {
	print REPORT $key, "\t", $stats->{$key},"\n";
    }
    foreach my $orf (sort {$a->[0] <=> $b->[0]} @$orfs) {
	print REPORT join("\t",$replicon,@$orf),"\n";
    }

    close REPORT;
    warn "$output_filename written\n";
}

sub stats {
    my ($orfs)=@_;
    my %totals;
    foreach my $strand ('+','-') {
	my ($start2codon, $stop2codon)=hashify_predictions($orfs,$strand);
	$totals{B}+=scalar keys %$start2codon;
	compare_predictions($strand,$start2codon,$stop2codon,\%totals);
    }

    $totals{Sn}=$totals{TP}/$totals{ncbi};
    $totals{sSn}=($totals{TP}+$totals{sTP})/$totals{ncbi};
    $totals{FOR}=$totals{FN}/$totals{ncbi};
    $totals{PPV}=$totals{TP}/$totals{B};
    $totals{sPPV}=($totals{TP}+$totals{sTP})/$totals{B};
    $totals{FDR}=$totals{FP}/$totals{B};
    warn Dumper(\%totals);
    return wantarray? %totals:\%totals;
}

sub compare_predictions {
    my ($strand,$start2codon,$stop2codon,$totals)=@_;
    my (@TPs,@sTPs,@FNs);
    my $ncbi_codons_file=$filename;
    $ncbi_codons_file=~s/\.fna/.ptt/;
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


#    write_results("True Positives","TPs",$strand,\@TPs);
#    write_results("semi-True Positives","sTPs",$strand,\@sTPs);
#    write_results("False Positives","FPs",$strand,\@FPs);
#    write_results("False Negatives","FNs",$strand,\@FNs);

    $totals->{TP}+=$nTP;
    $totals->{sTP}+=$nsTP;
    $totals->{FP}+=$nFP;
    $totals->{FN}+=$nFN;
}

sub write_results {
    my ($title, $short_title, $strand, $list)=@_;
    my $filename="something.$strand.${short_title}";
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
    my $strand=shift or confess "no strand";

    my $start2codon={};
    my $stop2codon={};
    foreach my $codon (@$predictions) {
	my ($start,$stop,$strand)=@$codon;
	my $codon=[$start,$stop];
	$start2codon->{$start}=$codon;
	$stop2codon->{$stop}=$codon;
    }
    ($start2codon,$stop2codon);
}

main(@ARGV);

