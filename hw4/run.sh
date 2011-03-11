cd bin

replicon=$1
prot_file=${replicon}.faa
alignment_file=hw2-muscle17.txt
bps_file=${replicon}.bps.ser
java ProfileHMM $prot_file $alignment_file $bps_file $use_alignment
