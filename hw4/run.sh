cd bin

# prot_file=NC_011660.faa
# alignment_file=hw2-muscle17.txt
# bps_file=NC_011660.bps.ser
# java ProfileHMM $prot_file $alignment_file $bps_file $use_alignment

prot_file=hw2-muscle17.txt.veryshort.faa
alignment_file=hw2-muscle17.txt.veryshort
bps_file=NC_011660.bps.ser
java ProfileHMM $prot_file $alignment_file $bps_file $use_alignment
