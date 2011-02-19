// Really sad that we can't modify HCR unless we want to re-run FindHCRs (or write a text-based parser)
// Instead, create a new class that has an HCR as a member.
// Sadly, though, we have to manually pass off all the methods that we need in HCR, since we can't inherit from it 
// (since we're already inheriting it from GenomeFeature)

class HCRFeature extends GenomeFeature {
    public HCR hcr;

    public HCRFeature(HCR hcr) {
	this.hcr=hcr;
    }

    public String toString() { return hcr.toString(); }
    public String fullString() { return hcr.fullString(); }
    // we'll see what else we actually need...
}