#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;
use RaCodons;
use Test::More qw(no_plan);

use vars qw($rac $start2codon $stop2codon $ncbi_start2codon $ncbi_stop2codon $ncbi_orfs);

BEGIN: {
  Options::use(qw(d q v h fuse=i min_codons:i skip_verify_fps));
    Options::useDefaults(fuse => -1,
			 min_codons=>125,
	);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


sub main {
    my $results_filename=shift or die "no results (.txt) file\n";
    my $fna_filename='../../'.$results_filename;
    $fna_filename=~s/\.txt/.fna/;

    # load NCBI predictions:
    my $ptt_filename=$fna_filename;
    $ptt_filename=~s/\.fna/.ptt/;
    load_ncbi($ptt_filename);

    my $report_filename=$results_filename;
    $report_filename=~s/\.txt/.report/;
    open (REPORT,">$report_filename") or die "Can't open $report_filename for writing\n";

    $rac=RaCodons->new(filename=>"$fna_filename.p");

    open (RESULTS,$results_filename) or die "Can't open $results_filename: $!\n";
    warn "reading $results_filename...\n";
    my $n_seq=0;
    my $n_good=0;
    while (<RESULTS>) {
	chomp;
	my ($replicon,$start,$stop,$strand)=split(/\s+/);
	next unless $strand && $strand=~/^[-+]$/; # weed out comments, stats, etc.
	$n_seq++;

	$start2codon->{key2($start,$strand)}=[$start,$stop,$strand];
	$stop2codon->{key2($stop,$strand)}=[$start,$stop,$strand];
	next if is_true_pos($start,$stop,$strand);

	unless ($options{skip_verify_fps}) {
	    my $seq=$rac->seq_at1($start,$stop); # ignore strand here...
	    $seq=reverse RaCodons::comp($seq) if $strand eq '-'; # ...but fix it here

	    my $seq_ok=verify_seq($seq);
	    unless (all_ok($seq_ok)) {
		print REPORT "$start\t$stop\t$strand\t$seq\n";
		print REPORT Dumper($seq_ok);
	    }
	    $n_good+=$seq_ok;
	}

#	next if is_semi_true_pos($start,$stop,$strand);


	# 
    }
    close RESULTS;
    print "$n_seq seqs, $n_good ok\n";

    # find out how many false negatives were due to a different start codon or too short
    my ($n_d,$n_s)=check_false_negatives();
    print REPORT "\nFalse Negatives:\n";
    print REPORT "$n_d with different start codon\n";
    print REPORT "$n_s too short\n";

    close REPORT;
    warn "$report_filename written\n";
}

sub all_ok {
    my $hash=shift;
    my $ok=1;
    foreach my $v (values %$hash) {
	$ok &&= $v;
    }
    $ok;
}

sub verify_seq {
    my ($seq)=@_;

    my %ok;
    $ok{mod}=is((length $seq)%3,0,"length a multiple of 3");
    $ok{len}=ok(length $seq > $options{min_codons}*3, "seq is long enough");
    $ok{start}=is(substr($seq,0,3),is_start(),"starts with start codon");

    if (length($seq) > $options{min_codons}*3) {
	my $tail_len=length($seq)-($options{min_codons}*3);
	my $tail=substr($seq,-$tail_len);
	my @codons=$tail=~/\w\w\w/g;
	my $stop=pop @codons;
	$ok{stop}=ok(is_stop($stop),"got stop codon ($stop)");
	my $no_stop=1;
	foreach my $codon (@codons) {
	    $no_stop &&= ok(! is_stop($codon),"not a stop codon: ($codon)");
	}
	$ok{no_stop}=$no_stop;
    }

    wantarray? %ok:\%ok;
}

sub is_start { $_[0]='ATG' }
sub is_stop { $_[0]=~/^TAG|TAA|TGA$/ }
sub key2 { join('_',@_); }

sub is_true_pos {
    my ($start,$stop,$strand)=@_;
    return 0 unless is_semi_true_pos($start,$stop,$strand);
    return exists $ncbi_start2codon->{key2($start,$strand)};
}

sub is_semi_true_pos {
    my ($start,$stop,$strand)=@_;
    return exists $ncbi_stop2codon->{key2($stop,$strand)};
}

sub is_false_negative {
    my ($start,$stop,$strand)=@_; # these must come from ncbi, not our predictions
    !($start2codon->{key2($start,$strand)} && $stop2codon->{key2($stop,$strand)});
}

# load ncbi predictions
sub load_ncbi {
    my ($ncbi_prot_file)=@_;
    warn "loading $ncbi_prot_file...\n";

    ($ncbi_start2codon,$ncbi_stop2codon)=({},{});
    $ncbi_orfs=[];

    open (NCBI,$ncbi_prot_file) or die "Can't open $ncbi_prot_file: $!\n";
    while (<NCBI>) {
	chomp;
	next unless /^(\d+)\.\.(\d+)\s+([-+])/;
	my ($start,$stop,$strand)=($1,$2,$3);
	my $codon=[$start,$stop,$strand];
	push @$ncbi_orfs,$codon;
	$ncbi_start2codon->{key2($start,$strand)}=$codon;
	$ncbi_stop2codon->{key2($stop,$strand)}=$codon;

    }
    close NCBI;
    
    ($ncbi_orfs,$ncbi_start2codon,$ncbi_stop2codon);
}

# why didn't we find everything?
sub check_false_negatives {
    my ($n_different_start,$n_short)=(0,0);
    warn "checking false negatives...\n";

    foreach my $orf (@$ncbi_orfs) {
	next unless is_false_negative(@$orf);

	my ($start,$stop,$strand)=@$orf;
	my $seq=$rac->seq_at1($start,$stop); # ignore strand here...
	$seq=reverse RaCodons::comp($seq) if $strand eq '-'; # ...but fix it here

	$n_different_start++ if !is_start(substr($seq,0,3));
	$n_short++ if length($seq)<$options{min_codons}*3;
    }

    return ($n_different_start,$n_short);
}



main(@ARGV);

