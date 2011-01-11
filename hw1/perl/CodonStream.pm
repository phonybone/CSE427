package CodonStream;
use strict;
use warnings;

use HasAccessors qw(:all);
use base qw(HasAccessors);
add_accessors(qw(filename fh buffer buffer_len i codon));
use FileHandle;
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

sub next {
    my ($self)=@_;
    if ($self->i >= ($self->buffer_len()-2)) {
	my $prefix=substr($self->buffer,-2);
	return undef unless $self->_next_line($prefix);
    }
    my $i=$self->i;
    my $codon=substr($self->buffer,$i,3);
    $self->i($i+1);
    $self->codon($codon);
}

sub open {
    my $self=shift;
    my $filename=shift || $self->filename or confess "no filename";

    $self->fh(FileHandle->new());
    $self->fh->open($filename) or die "Can't open $filename: $!\n";
    $self->_next_line;
    $self->_next_line unless $self->buffer=~/^[acgt]+$/i;


}

sub _next_line {
    my $self=shift;
    my $prefix=shift || '';
    my $line=$self->fh->getline;
    if ($line) {
	chomp $line;
	$self->i(0);
	$self->buffer($prefix.$line);
	$self->buffer_len(length $self->buffer);
    }
    $line;
}



sub DESTROY {
    my ($self)=@_;
    $self->fh->close();
}


1;
