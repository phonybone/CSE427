#-*-perl-*-

use RaCodons;
use Data::Dumper;

my $filename='/home/victor/random/coursework/CSE427/NC_011660.fna.20p';
my $rac=RaCodons->new(filename=>$filename);

foreach my $i (0..20) {
    printf "%d-%d: %s\n", $i, $i+2, $rac->seq_at($i,$i+2);
    printf "%d-%d: %s\n", $i+1, $i+3, $rac->seq_at1($i+1,$i+3);

    printf "%d-%d: %s -\n", $i+1, $i+3, $rac->seq_at1($i+1,$i+3,'-');

}
