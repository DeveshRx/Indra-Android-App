package devesh.ephrine.workmanager;

import static devesh.ephrine.util.ContactUtil.getContactName;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.List;

import devesh.common.ViewModel.AppDataViewModel;
import devesh.common.database.contactsdb.ContactAppDatabase;
import devesh.common.database.contactsdb.ContactUser;
import devesh.ephrine.R;
import devesh.ephrine.util.ContactUtil;

public class ContactsUpdateWorkManager extends Worker {

    static final String TAG = "contactUpdate: ";
    FirebaseDatabase database;
    DatabaseReference contactsDBRef;
    FirebaseAuth mAuth;
    ContactAppDatabase contactsDB;
    AppDataViewModel contactsLiveModel;

    List<ContactUser> contactUserList;
    Context mContext;

    public ContactsUpdateWorkManager(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);

        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        contactsDBRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/indra/contacts/");
        contactsDB = Room.databaseBuilder(context, ContactAppDatabase.class, context.getString(R.string.DATABASE_CONTACTS_DB))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigration()
                .build();

        contactsLiveModel = new ViewModelProvider(ViewModelStore::new).get(AppDataViewModel.class);

        contactUserList=new ArrayList<>();

    }



    @Override
    public Result doWork() {

        ValueEventListener contactListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                //Post post = dataSnapshot.getValue(Post.class);

                if (dataSnapshot != null) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        ContactUser c = new ContactUser();
                        c.phone = data.child("phone").getValue(String.class);
                        c.UID = data.child("uid").getValue(String.class);
                        c.DisplayName = null;
                        if (data.child("name").getValue(String.class) != null) {
                            c.DisplayName = data.child("name").getValue(String.class);
                        } else {
                            Log.d(TAG, "onDataChange: DB NAME NULL");
                                try {
                                    // phone must begin with '+'
                                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                                    Phonenumber.PhoneNumber numberProto = phoneUtil.parse(c.phone, "");
                                    int countryCode = numberProto.getCountryCode();
                                    long nationalNumber = numberProto.getNationalNumber();
                                    Log.d(TAG, "code " + countryCode);
                                    Log.d(TAG, "national number " + nationalNumber);
                                    String name=getContactName(mContext, c.phone);
                                    if(name.equals(null) ||name.equals("")){
                                        name= getContactName(mContext, String.valueOf(nationalNumber));
                                    }
                                    c.DisplayName = name;
                                } catch (Exception e) {
                                    Log.e(TAG, "onDataChange: ERROR #45453 "+e );
                                }
                        }

                        c.photo = null;
                        if (data.child("photo").getValue(String.class) != null) {
                            c.photo = data.child("photo").getValue(String.class);
                        }

                        c.isAppUser = 1;
                        contactUserList.add(c);
                        //contactsDB.contactDao().insertRawContacts(c);
                    }

                    contactsDB.contactDao().insertAll(contactUserList);
                    Log.d(TAG, "onDataChange: FINISHED: ");
                    contactsLiveModel.getAppContactsList().postValue(contactUserList);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

            }
        };
        contactsDBRef.addListenerForSingleValueEvent(contactListener);

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }


}
