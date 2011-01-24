use strict;
use warnings;
use Carp;
use Data::Dumper;
use File::Basename;
 
use Options;
use CodonStream;

use vars qw($usage);

BEGIN: {
    Options::use(qw(d q v h min_codons:i fuse:i no_header no_save_orfs save_lists replicon|r:s));
    Options::useDefaults(min_codons=>125,
			 replicon=>[],
			 fuse => -1,
	);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};

    my $script=basename($0);
    $usage="$script <filename> [options]\n";
}


my @bugs=(
    {replicons=>['NC_011660'],
     name=>'Listeria monocytogenes HCC23',
     orf_file=>'personalORF.txt',
     stats_file=>'personalStats.txt',
     stats=>{},
     answers=>['B',
	       'pathogenic; causes listeriosis',
	       'decaying vegetable matter, sewage, water, and soil',
	       '2.94453',
	       '38.0',
	       '2974',
	       '888',
	       '89'],

    },
    
    {replicons=>['NC_007618','NC_007624'],
     name=>'Brucella melitensis biovar Abortus 2308',
     orf_file=>'community.txt',
     stats_file=>'community.txt',
     stats=>{},
     answers=>[],
    },
    );



sub main {
    foreach my $bug (@bugs) {
	process_bug($bug);
    }
}



sub process_bug {
    my ($bug)=@_;

    my @reps=@{$options{replicon}};
    @reps= all_replicons() unless @reps;
    

    my $do_report=0;
    foreach my $replicon (@{$bug->{replicons}}) {
	next unless grep {$replicon eq $_} @reps;
	$do_report=1;
	warn "processing $replicon...\n";

	$bug->{orfs}=[] unless defined $bug->{orfs};

	# do '+' strand:
	my $fna_file="$replicon.fna";
	my $strand_orfs_p=read_strand($fna_file,'+');
	push @{$bug->{orfs}},@$strand_orfs_p;

	# do '-' strand: have to generate rc first:
	my ($rc_filename,$gs)=write_rev_comp($fna_file);
	my $strand_orfs_n=read_strand($rc_filename,'-',$gs);
	push @{$bug->{orfs}},@$strand_orfs_n;


	my $stats=stats($bug,$replicon);
    }

    if ($do_report) {
	unlink $bug->{orf_file};
	unlink $bug->{stats_file};
	report_orfs($bug);
	report_stats($bug);
    }
}

sub write_rev_comp {
    my $fna_file=shift or die "no filename";
    open (INPUT,$fna_file) or die "Can't open $fna_file: $!";
    open (OUTPUT,">$fna_file.rev") or die "Can't open $fna_file.rev for writing: $!";

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
    warn "$fna_file.rev written\n" unless $options{q};
    return ("$fna_file.rev",$gs);
}

sub revcomp {
    my $seq=shift;
    $seq=~tr/ACGT/TGCA/;
    $seq=~tr/acgt/tgca/;
    return scalar reverse $seq;	# perl list/scalar context can be weird sometimes
}


# returns $orfs
# Each element: [$replicon,$start,$stop,$strand]
sub read_strand {
    my ($fna_file,$strand,$gs)=(@_);
    die "no gs" if $strand eq '-' and not defined $gs;

    my $cs=CodonStream->new(filename=>$fna_file);
    my $orfs=find_orfs($cs,$strand,$gs,1);   
}

# returns a list[ref] of codom elements ([$start,$stop,$strand])
sub find_orfs {
    my ($cs,$strand,$gs,$use_saved_orfs)=@_;
    die "no gs" if $strand eq '-' and not defined $gs;
    my @orfs;			# results go here

    # check for previous results:
    my $orfs_file=$cs->filename;
    $orfs_file=~s/\.fna/.orfs/;
    if ($use_saved_orfs && -e $orfs_file) {
	if (open(ORFS,$orfs_file)) {
	    warn "using saved orfs in $orfs_file\n" unless $options{q};
	    while (<ORFS>) {
		my @orf=split(/\s+/);
		push @orfs,\@orf if ($orf[3] eq $strand);
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
    my $replicon=(split(/\./,$cs->filename))[0];
    $replicon=~s/\.rev//;	# hack
    
    while (my $codon=$cs->next) {
	my $frame=(($n-1)%3)+1;	# frames are 1-based
#	warn "$n codons...\n" if $n%10000==0;

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

	    push @orfs,[$replicon,$start,$stop,$strand];
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
	    warn "$orfs_file written\n" unless $options{q};
	}
    }

    return wantarray? @orfs:\@orfs;
}


sub is_stop { $_[0]=~/^TAG|TAA|TGA$/ }

sub report_stats {
    my ($bug)=@_;
    my @report_order=qw(TP sTP FN FP Sn sSn FOR PPV sPPV FDR);
    
    my $report_filename=$bug->{stats_file};
    open (REPORT,">>$report_filename") or die "Can't open $report_filename for writing: $!";

    print REPORT "Victor Cassen\n";
    print REPORT $bug->{name},"\n";
    my $stats=$bug->{stats};
    foreach my $key (@report_order) {
	printf REPORT "%.4f\n",$stats->{$key};
    }

    foreach my $a (@{$bug->{answers}}) { # may be an empty list
	print REPORT "$a\n";
    }

    close REPORT;

} 

sub report_orfs {
    my ($bug)=@_;

    my $report_filename=$bug->{orf_file};
    open (REPORT,">>$report_filename") or die "Can't open $report_filename for writing: $!";

    my @sorted=sort {
	($a->[0] cmp $b->[0]) ||
	    ($a->[1] <=> $b->[1])
    } @{$bug->{orfs}};
    
    foreach my $orf (@sorted) {
	print REPORT join("\t",@$orf),"\n";
    }
    
    close REPORT;
    warn "$report_filename written\n" unless $options{q};
}

# Generate stats for an entire replicon:
# Returns a stats hash[ref]
# $totals{A} is the number proteins listed by NCBI (aka $totals{ncbi})
# $totals{B} is the number of orfs predicted by me
sub stats {
    my ($bug,$replicon)=@_;
    my $orfs=$bug->{orfs};
    my $totals=$bug->{stats};

    foreach my $strand ('+','-') {
	my ($start2codon, $stop2codon)=hashify_predictions($orfs,$strand);
	$totals->{B}+=scalar keys %$start2codon;
	compare_predictions($strand,$start2codon,$stop2codon,$totals,$replicon,$bug);
    }

    $totals->{Sn}=$totals->{TP}/$totals->{ncbi};
    $totals->{sSn}=($totals->{TP}+$totals->{sTP})/$totals->{ncbi};
    $totals->{FOR}=$totals->{FN}/$totals->{ncbi};
    $totals->{PPV}=$totals->{TP}/$totals->{B};
    $totals->{sPPV}=($totals->{TP}+$totals->{sTP})/$totals->{B};
    $totals->{FDR}=$totals->{FP}/$totals->{B};
    warn "$replicon:\n",Dumper($totals) if $ENV{DEBUG};

    write_results($bug,$replicon) if $options{save_lists};

    return wantarray? %$totals:$totals;
}

sub compare_predictions {
    my ($strand,$start2codon,$stop2codon,$totals,$replicon,$bug)=@_;
    
    my $TPs=$bug->{TPs};
    my $sTPs=$bug->{sTPs};
    my $FNs=$bug->{FNs};
    my $FPs=$bug->{FPs};

    my $ncbi_codons_file="$replicon.ptt";
    open (NCBI,$ncbi_codons_file) or die "Can't open $ncbi_codons_file: $!";
    while (<NCBI>) {
	chomp;
	my ($range,$pstrand,$junk)=split(/\s+/);
	next unless $pstrand eq $strand;
	next unless $range=~/^(\d+)..(\d+)$/;
	$totals->{ncbi}+=1;
	my ($start,$stop)=($1,$2);
	my $codon=[$start,$stop,$pstrand];

	if ($start2codon->{$start} && $stop2codon->{$stop}) { # if we found both start & stop, TP
	    push @$TPs, $codon;
	    delete $start2codon->{$start};
	    delete $stop2codon->{$stop};
	} elsif ($stop2codon->{$stop}) { # if we found the stop codon, but not the start, it's a sTP
	    push @$sTPs, $codon;
	    delete $stop2codon->{$stop};
	} else {
	    push @$FNs, $codon;
	}
    }
    close NCBI;

    # the remaining entries in %$stop2codon are the FPs:
    my @FPs=grep {$_->[2] eq $strand} values %$stop2codon;
    push @$FPs,@FPs;
    my $nTP=scalar @$TPs;
    my $nsTP=scalar @$sTPs;
    my $nFP=scalar @$FPs;
    my $nFN=scalar @$FNs;

    print "\nstrand: $strand\n";
    printf "%d TPs\n",  $nTP;
    printf "%d sTPs\n", $nsTP;
    printf "%d FPs\n",  $nFP;
    printf "%d FNs\n",  $nFN;


    push @{$bug->{TP}},@$TPs;
    push @{$bug->{sTP}},@$sTPs;
    push @{$bug->{FP}},@$FPs;
    push @{$bug->{FN}},@$FNs;

    $totals->{TP}+=$nTP;
    $totals->{sTP}+=$nsTP;
    $totals->{FP}+=$nFP;
    $totals->{FN}+=$nFN;
}


sub write_results {
    my ($bug,$replicon)=@_;

    if ($options{save_lists}) {
	write_result_file("True Positives","TPs",$replicon,$bug->{TP});
	write_result_file("semi-True Positives","sTPs",$replicon,$bug->{sTP});
	write_result_file("False Positives","FPs",$replicon,$bug->{FP});
	write_result_file("False Negatives","FNs",$replicon,$bug->{FN});
    }
}

sub write_result_file {
    my ($title, $short_title, $replicon, $list)=@_;
    my $filename="$replicon.${short_title}";

    
    

    open (RESULTS,">$filename") or die "Can't open $filename for writing: $!\n";
    print RESULTS "# $title\n";
    foreach my $codon (sort {$a->[0] <=> $b->[0]} @$list) {
	if (@$codon != 3) {
	    die "bad codon in $title?\n",Dumper($codon);
	}
	printf RESULTS "%d..%d\t%s\t%d\n",@$codon, ($codon->[1]-$codon->[0]+1);
    }
    close RESULTS;
    warn sprintf("$filename written (%d)\n", scalar @$list) unless $options{q};
}

sub hashify_predictions {
    my $predictions=shift or die "no predictions";
    my $strand=shift or confess "no strand";

    my $start2codon={};
    my $stop2codon={};
    foreach my $codon (@$predictions) {
	my ($replicon,$start,$stop,$strand)=@$codon;
	my $codon=[$start,$stop,$strand];
	$start2codon->{$start}=$codon;
	$stop2codon->{$stop}=$codon;
    }
    ($start2codon,$stop2codon);
}

sub write_personal_stats {
    my ($bug)=@_;
    my $filename="personalStats.txt";
    open (FILE,">$filename") or die "Can't open $filename for writing: $!\n";
    print FILE $bug->{name},"\n";
    
    close FILE;
}


main(@ARGV);

