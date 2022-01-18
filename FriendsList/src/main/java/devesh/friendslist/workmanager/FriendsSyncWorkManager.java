package devesh.friendslist.workmanager;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import devesh.common.ViewModel.AppDataViewModel;
import devesh.common.database.contactsdb.ContactAppDatabase;
import devesh.common.database.contactsdb.ContactUser;
import devesh.common.utils.CachePref;
import devesh.common.utils.HttpsReq;
import devesh.friendslist.R;


public class FriendsSyncWorkManager extends Worker {
    String TAG = "FriendsSyncWM: ";
    Context mContext;
    ContactAppDatabase contactsDB;
    Gson gson;
    ArrayList<String> ContactsListArray;
    AppDataViewModel contactsLiveModel;
    List<ContactUser> FriendList;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseAuth mAuth;
CachePref cachePref;
HttpsReq httpsReq;
String fileObjURL;

    public FriendsSyncWorkManager(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);

        mContext = context;
        contactsDB = Room.databaseBuilder(mContext, ContactAppDatabase.class, mContext.getString(R.string.DATABASE_CONTACTS_DB))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigration()
                .build();
        gson = new Gson();
        ContactsListArray = new ArrayList<>();
        FriendList = new ArrayList<>();
        contactsLiveModel = new ViewModelProvider(ViewModelStore::new).get(AppDataViewModel.class);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
cachePref=new CachePref(mContext);
httpsReq=new HttpsReq(mContext);

    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.
        //     uploadImages();

        ContentResolver cr = mContext.getContentResolver();

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

  /*      Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null,null);
*/
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                        phoneNo = phoneNo.replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "");

                        ContactsListArray.add(phoneNo);
                        addToContactDB(name, phoneNo);

                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }

        // Save to File
        String fileName = "contacts.txt";
        String fileContent = gson.toJson(ContactsListArray);
        byte[] ff = fileContent.getBytes();

        File file = new File(mContext.getFilesDir(), fileName);
        if (file.exists()) {

        } else {

        }
        try {
            FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(fileContent.getBytes());
            fos.close();
            uploadContacts(file);
        } catch (Exception e) {
            Log.e(TAG, "doWork: ERROR: ", e);
        }


        contactsLiveModel.getAppContactsList().postValue(FriendList);
        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    void addToContactDB(String name, String phone) {
        ContactUser contact = new ContactUser();

        contact.phone = phone;
        contact.DisplayName = name;
        contactsDB.contactDao().insertRawContacts(contact);
        FriendList.add(contact);
    }

    void uploadContacts(File mFile) {
        String CountryCode="+00";
        try{
            CountryCode=cachePref.getString(mContext.getString(R.string.Pref_Country_Code_with_Plus));

        }catch (Exception e){
            Log.e(TAG, "uploadContacts: ERROR #678 "+e );

        }

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCacheControl("no-cache")
                .setCustomMetadata("app", "indra")
                .setCustomMetadata("file_type", "contact")
                .setCustomMetadata("owner_id", mAuth.getCurrentUser().getUid())
                .setCustomMetadata("owner_phone", mAuth.getCurrentUser().getPhoneNumber())
                .setCustomMetadata("epoch", String.valueOf(System.currentTimeMillis()))
               .setCustomMetadata("country_code",CountryCode)
                .build();

        Uri file = Uri.fromFile(mFile);

        fileObjURL="indra/users/" + mAuth.getCurrentUser().getUid() + "/indra_" + file.getLastPathSegment();

        StorageReference riversRef = storageRef.child(fileObjURL);

        UploadTask uploadTask = riversRef.putFile(file, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "onFailure: Contact Upload ");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG, "onSuccess: Contacts Upload Success Sync ");
                SendSyncRequest();
            }
        });


    }

    void SendSyncRequest(){
        HashMap<String,String> hm=new HashMap<>();
        hm.put("uid",mAuth.getCurrentUser().getUid());
        hm.put("file",fileObjURL);

        String body=gson.toJson(hm);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable(){
            @Override
            public void run(){
                try{
                    String response= httpsReq.post(mContext.getString(R.string.URL_SyncContacts), body);
                    Log.d(TAG, "SendSyncRequest: RESPONSE: "+response);
                }catch (Exception e){
                    Log.e(TAG, "SendSyncRequest: ERROOR #76809 "+e );
                }
            }
        });


    }


  /*  private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }
*/
}