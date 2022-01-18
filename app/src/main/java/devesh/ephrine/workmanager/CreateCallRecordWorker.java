package devesh.ephrine.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import devesh.ephrine.R;
import devesh.common.utils.EpochLib;
import devesh.ephrine.rooms.CallHistory.CallRecord;
import devesh.ephrine.rooms.CallHistory.CallRecordsAppDatabase;

public class CreateCallRecordWorker extends Worker {

    CallRecordsAppDatabase CallRecordDB;
    Context mContext;
    String TAG = "CallLogWorker";

    public CreateCallRecordWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
        CallRecordDB = Room.databaseBuilder(mContext, CallRecordsAppDatabase.class, mContext.getString(R.string.DATABASE_CALL_RECORDS))
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    @Override
    public Result doWork() {
        EpochLib el = new EpochLib();
        CallRecord callRecord = new CallRecord();
        callRecord.UID = getInputData().getString("uid");
        callRecord.io = getInputData().getString("io");
        callRecord.time_epoch = el.getEPOCH();
        callRecord.time_formatted = el.getEPOCHFormatted(callRecord.time_epoch, "dd-MM-yyyy");
        CallRecordDB.callRecordsDAO().insertAll(callRecord);
        Log.d(TAG, "doWork: callRecord.UID:" + callRecord.UID + "\ncallRecord.io:" + callRecord.io +
                "\ncallRecord.time_epoch:" + callRecord.time_epoch + "\ncallRecord.time_formatted:" + callRecord.time_formatted);

        return Result.success();
    }
}