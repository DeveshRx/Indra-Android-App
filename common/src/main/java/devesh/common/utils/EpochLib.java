package devesh.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EpochLib {
    public long getEPOCH() {
        return System.currentTimeMillis();
    }

    /*
 Format   "dd/MM/yyyy"
     */
    public String getEPOCHFormatted(long epoch, String Format) {
        if (Format == null) {
            Format = "dd/MM/yyyy";
        }
  /*      else if (Format.equals("dd/MM/yyyy")) {

        }else if (Format.equals("dd-MM-yyyy")) {

        } else if(Format.equals("dd-MM-yyyy HH:mm")){

        }else {
            Format = "dd/MM/yyyy";
        }
*/
        SimpleDateFormat sdf = new SimpleDateFormat(Format);
        sdf.format(new Date(epoch));
        String date = sdf.format(new Date(epoch));
        return date;
    }
    public long convert2Seconds(long EPOCH){
        long timestampSeconds = TimeUnit.MILLISECONDS.toSeconds(EPOCH);
return timestampSeconds;
    }
}
