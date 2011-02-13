#!/usr/bin/env perl 
use strict;
use warnings;
use Carp;
use Data::Dumper;
 
use Options;

BEGIN: {
  Options::use(qw(d q v h fuse=i));
    Options::useDefaults(fuse => -1);
    Options::get();
    die Options::usage() if $options{h};
    $ENV{DEBUG} = 1 if $options{d};
}


sub main {
    my $prot_file=shift or die "no prot_file";
    my $index=index_prots($prot_file);
    warn sprintf "index: %d keys\n", scalar keys %$index;

    # write index to disk
    my $index_file=$prot_file;
    $index_file=~s/\.[^.]*$/.index/;
    open (INDEX,">$index_file") or die "Can't write to $index_file: $!\n";
    print INDEX Dumper($index);
    close INDEX;
    warn "$index_file written\n";
}

sub dump_index {
    my $index=shift;
    while (my ($k,$v)=each %$index) {
	warn "$k:\n",Dumper($v);
    }

}

sub index_prots {
    my $prot_file=shift or die "no prot_file";
    my $index={};
    open (PROTS,$prot_file) or die "no prot_file";
    my ($prot_name, $prot_seq, $prot_id);
    while (<PROTS>) {
	chomp;
	if (/^>/) {
	    if ($prot_name) {
#		warn "$prot_name\n";
		index_prot($index,$prot_id,$prot_seq);
	    }
	    $prot_name=$_;
	    $prot_id=(split(/\|/,$prot_name))[3];
	    $prot_seq='';
	} else {
	    $prot_seq.=$_;
	}
    }

    index_prot($index,$prot_id,$prot_seq); # last one
    close PROTS;
    $index;
}

# 
sub index_prot {
    my ($index, $prot_id, $prot_seq)=@_;
    my $l=length($prot_seq)-2;
    warn "$prot_id: $l\n";
    my $i=0;
    while ($i<$l) {
	my $t3=substr($prot_seq,$i,3);
	$index->{$t3}->{$prot_id}++;
	$i++;
    }
}

main(@ARGV);

