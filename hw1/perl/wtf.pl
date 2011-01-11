#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use lib '/home/victor/git/sandbox/perl/PhonyBone';
use Options;
use RaCodons;
use Test::More qw(no_plan);

BEGIN: {
  Options::use(qw(d q v h fuse=i min_codons:i));
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
    my $ptt_filename=$fna_filename;
    $ptt_filename=~s/\.fna/.ptt/;

    my $report_filename=$results_filename;
    $report_filename=~s/\.txt/.report/;
    open (REPORT,">$report_filename") or die "Can't open $report_filename for writing\n";

    my $rac=RaCodons->new(filename=>"$fna_filename.p");

    open (RESULTS,$results_filename) or die "Can't open $results_filename: $!\n";
    my $n_seq=0;
    my $n_good=0;
    while (<RESULTS>) {
	chomp;
	my ($replicon,$start,$stop,$strand)=split(/\s+/);
	next unless $strand && $strand=~/^[-+]$/;
	$n_seq++;

	my $seq=$rac->seq_at1($start,$stop); # ignore strand here...
	$seq=reverse RaCodons::comp($seq) if $strand eq '-'; # ...but fix it here
	my $seq_ok=verify_seq($seq);
	unless (all_ok($seq_ok)) {
	    print REPORT "$start\t$stop\t$strand\t$seq\n";
	    print REPORT Dumper($seq_ok);
	}
	$n_good+=$seq_ok;

#	next if true_pos($start,$stop);
#	next if semi_true_pos($start,$stop);
	
	# is a false positive: why?

	# 
    }
    close RESULTS;
    print "$n_seq seqs, $n_good ok\n";

    # find out how many false negatives were due to a different start codon or too short


    close REPORT;
    warn "$report_filename written\n";
    done_testing();
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


sub ncbi_predictions {
    my ($ncbi_prot_file)=@_;
    my $ncbi_start2codon;
    my $ncbi_stop_codon;

    open (NCBI,$ncbi_prot_file) or die "Can't open $ncbi_prot_file: $!\n";
    while (<NCBI>) {
	chomp;
	next unless /^(\d+)\.\.(\d+)\s+([-+])/;
	my ($start,$stop,$strand)=($1,$2,$3);
    }
    close NCBI;

}


main(@ARGV);

