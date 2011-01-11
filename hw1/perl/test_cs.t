#-*-perl-*-

use CodonStream;
use Data::Dumper;

my $filename='/home/victor/random/coursework/CSE427/NC_011660.fna.2';
my $cs=CodonStream->new(filename=>$filename);

while (my $codon=$cs->next) {
    print "$codon\n";
}
