#-*-perl-*-

use RaCodons;
use Data::Dumper;

my $filename='/home/victor/random/coursework/CSE427/NC_011660.fna.20p';
my $rac=RaCodons->new(filename=>$filename);

my $i=1;
while (my $codon=$rac->next) {
    my $ss='';
    $ss="(start) $i" if ($codon eq 'ATG');
    $ss="(stop) $i" if $codon=~/TAA|TAG|TGA/;
    print "$codon $ss\n";
    $i++;
}
