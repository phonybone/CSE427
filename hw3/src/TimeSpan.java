import java.util.Date;

class TimeSpan {
    private Date start;		// these suckers measure milliseconds since the epoch (with long ints)
    private Date end;

    private final long mSecsPerSec  =1000;
    private final long mSecsPerMin  =mSecsPerSec*60;
    private final long mSecsPerHour =mSecsPerMin*60;
    private final long mSecsPerDay  =mSecsPerHour*24;
    private final long mSecsPerYear =mSecsPerDay*365;

    public TimeSpan(Date start, Date end) {
	if (start.after(end)) {
	    Date tmp=start;
	    start=end;
	    end=tmp;		// sheesh
	}
	this.start=start;
	this.end=end;
    }
    
    public String toString() {
	long nMillis=end.getTime()-start.getTime();
	assert (nMillis>0);
	//	System.err.println(String.format("TS.toString (y): nMillis=%d", nMillis));
	
	long nYears=nMillis/mSecsPerYear;
	nMillis=nMillis%mSecsPerYear;
	//	System.err.println(String.format("TS.toString (d): nMillis=%d", nMillis));

	long nDays=nMillis/mSecsPerDay;
	nMillis=nMillis%mSecsPerDay;
	//	System.err.println(String.format("TS.toString (h): nMillis=%d", nMillis));

	long nHours=nMillis/mSecsPerHour;
	nMillis=nMillis%mSecsPerHour;
	//	System.err.println(String.format("TS.toString (m): nMillis=%d", nMillis));

	long nMins=nMillis/mSecsPerMin;
	nMillis=nMillis%mSecsPerMin;
	//	System.err.println(String.format("TS.toString (s): nMillis=%d", nMillis));

	long nSecs=nMillis/mSecsPerSec;
	nMillis=nMillis%mSecsPerSec;
	//	System.err.println(String.format("TS.toString (ms): nMillis=%d", nMillis));

	StringBuffer buf=new StringBuffer();
	if (nYears>0) { buf.append(String.format("%d years",nYears)); }
	if (nDays>0)  { buf.append(String.format(" %d days",nDays)); }
	if (nHours>0)  { buf.append(String.format(" %d hours",nHours)); }
	if (nMins>0)  { buf.append(String.format(" %d mins",nMins)); }
	if (nSecs>0)  { buf.append(String.format(" %d secs",nSecs)); }
	if (nMillis>0)  { buf.append(String.format(" %d msecs",nMillis)); }
	return new String(buf);
    }


    public static void main(String[] argv) {
	Date start=new Date();
	TimeSpan ts63=new TimeSpan(start,new Date(start.getTime()+63l));
	System.out.println(String.format("63 msecs: %s",ts63));

	TimeSpan ts2663=new TimeSpan(start,new Date(start.getTime()+2663l));
	System.out.println(String.format("2663 msecs: %s",ts2663));

	TimeSpan ts2=new TimeSpan(start,new Date(start.getTime()+232663l));
	System.out.println(String.format("232663 msecs: %s",ts2));

	TimeSpan ts3=new TimeSpan(start,new Date(start.getTime()+1232663l));
	System.out.println(String.format("1232663 msecs: %s",ts3));

	TimeSpan ts4=new TimeSpan(start,new Date(start.getTime()+12342663l));
	System.out.println(String.format("12342663 msecs: %s",ts4));

	TimeSpan ts5=new TimeSpan(start,new Date(start.getTime()+123452663l));
	System.out.println(String.format("123452663 msecs: %s",ts5));

	TimeSpan ts6=new TimeSpan(start,new Date(start.getTime()+1234562663l));
	System.out.println(String.format("1234562663 msecs: %s",ts6));

	TimeSpan ts7=new TimeSpan(start,new Date(start.getTime()+12345672663l));
	System.out.println(String.format("12345672663 msecs: %s",ts7));

	TimeSpan ts8=new TimeSpan(start,new Date(start.getTime()+123456782663l));
	System.out.println(String.format("123456782663 msecs: %s",ts8));

    }
}