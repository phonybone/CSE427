package RaCodons;
# random access codons

use strict;
use warnings;

use HasAccessors qw(:all);
use base qw(HasAccessors);
add_accessors(qw(filename genome i codon));
use Sys::Mmap;
use Data::Dumper;
use Carp;


sub new {
    my ($proto,%args)=@_;
    my $class = ref $proto || $proto;
    my $self=$class->SUPER::new(%args);

    # object initializition goes here as needed
    $self->open if $self->filename;

    $self;
}

# has to be a "pure" file (ie, only contains bases, nothing else)
sub open {
    my $self=shift;
    my $filename=shift || $self->filename or confess "no filename";
    die "$filename doesn't end in .p\n" unless $filename=~/\.p$/;

    open (GENOME,$filename) or die "can't open $filename: $!\n";
    my $genome;
    my $offset=0;
    my $size=0;
    my $flags=MAP_SHARED;
    my $prots=PROT_READ;
    mmap($genome, $size, $prots, $flags, GENOME, $offset) or die "mmap: $!\n";
    $self->genome($genome);
    $self->i(0);

    $self;
}

# return the sequence starting at $start and ending at $stop (inclusive)
sub seq_at {
    my ($self,$start,$stop)=@_;
    my $len=$stop-$start+1;
    my $genome=$self->genome;
    return substr($genome,$start,$len);
}

# 1-based version of above: if you ask for 1..39, you get 0..38 (inclusive)
sub seq_at1 {
    my ($self,$start,$stop)=@_;
    $start--;
    $stop--;
    my $len=$stop-$start+1;
    my $genome=$self->genome;
    return substr($genome,$start,$len);
}

# return codon starting at $genome[$offset] 
# ($offset in bp, 1-based)
sub codon_at {
    my ($self,$offset)=@_;
    $self->seq_at($offset-1,$offset+2);
}

sub next {
    my $self=shift;
    my $i=$self->i;
    my $codon=substr($self->genome,$i,3);
    $self->codon($codon);
    $self->i($i+1);
    $codon;
}

sub close {
    my $self=shift;
    if (my $genome=$self->genome) {
	munmap($genome);
	close GENOME;
    }
}

sub DESTORY {
    my $self=shift;
    $self->close;
}

1;
