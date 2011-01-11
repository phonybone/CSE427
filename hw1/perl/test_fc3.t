#-*-perl-*-
use warnings;
use strict;

use RaCodons;
use Data::Dumper;
use Test::More qw(no_plan);
use Options;

Options::use(qw(d q v h l:i gs:s n:1));
Options::useDefaults(l=>10,
    );
Options::get();

my $filename=sprintf '/home/victor/random/coursework/CSE427/NC_011660.fna.%sp', $options{gs};
warn "using $filename\n";
my $rac=RaCodons->new(filename=>$filename);

my $gs=$rac->gs_size;
my $len=$gs/10;
srand 10;

my $start=0;
my $stop=140;
my $seq=$rac->seq_at($start,$stop);
my $rcseq=$rac->seq_at($gs-($stop+1),$gs-($start+1),'-');
print "\n$seq\n$rcseq\n\n";

$start++; $stop++;
$seq=$rac->seq_at1($start,$stop);
$rcseq=$rac->seq_at1($gs-($stop+1),$gs-($start+1),'-');
print "$seq\n$rcseq\n\n";


my $n=$options{n};
while ($n) {
    my $start=int(rand($gs))+1;
    my $stop=$start+int(rand($len))+$len; # 
    next if $stop > $rac->gs_size;	# unlikely, unless (really) small genome

    my $seq=   $rac->seq_at($start,        $stop);
    my $rc_seq=$rac->seq_at($gs-($stop+1),$gs-($start+1),'-');
    my $cseq=RaCodons::comp($seq);

    is($seq,reverse(RaCodons::comp($rc_seq)),"$start-$stop: sequences match");

    print "\n$seq s\n$cseq c\n$rc_seq rc\n\n";
    $n--;
}
