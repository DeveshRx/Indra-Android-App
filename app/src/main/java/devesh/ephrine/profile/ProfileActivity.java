package devesh.ephrine.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import devesh.ephrine.R;
import id.zelory.compressor.Compressor;
import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

public class ProfileActivity extends AppCompatActivity {
    EasyImage easyImage;

    FirebaseAuth mAuth;
    FirebaseUser fUser;

    CircleImageView ProfileIMG;

    TextInputEditText UserNameET;
    TextInputEditText EmailIDET;
    TextInputEditText UserBioET;

    TextView PhoneNoTV;

    String UserName = null;
    String EmailID = null;
    String PhotoURL = null;
    String UserBio = null;
    String TAG = "ProfileActivity: ";

    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseDatabase database;

    DatabaseReference UserBioDB;
    String firebase_project_id;
    View savingView;
    File ProfilePicFile;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.BG_Blank)));

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        database = FirebaseDatabase.getInstance();

        firebase_project_id = FirebaseApp.getInstance().getOptions().getProjectId();

        PhotoURL = "https://storage.googleapis.com/" + firebase_project_id + ".appspot.com/indra/users/" + fUser.getUid() + "/app_user_photo.jpg";

        UserNameET = findViewById(R.id.UserNameET);
        EmailIDET = findViewById(R.id.UserEmailET);
        ProfileIMG = findViewById(R.id.ProfileImageView);
        PhoneNoTV = findViewById(R.id.PhoneNoTextView);
        savingView = findViewById(R.id.savingView);
        savingView.setVisibility(View.GONE);

        UserBioET = findViewById(R.id.UserBioET);

        easyImage = new EasyImage.Builder(this)
                .setChooserTitle("Pick Photo")
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setCopyImagesToPublicGalleryFolder(false)
                .setFolderName("indra")
                .allowMultiple(false)
                .build();
        UserBioDB = database.getReference("users/" + fUser.getUid() + "/profile/indraBio");
        ProfilePicFile = new File(getFilesDir(), FILE_PROFILE_PIC);

        LoadProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.menu_signout) {
            signout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signout() {
        Toast.makeText(this, "Signing Out", Toast.LENGTH_SHORT).show();

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        Toast.makeText(ProfileActivity.this, "You have Signed Out", Toast.LENGTH_LONG).show();
                        deleteAppData();
                        finish();
                    }
                });

        // FirebaseAuth.getInstance().signOut();


    }

    private void deleteAppData() {
        try {
            // clearing app data
            String packageName = getApplicationContext().getPackageName();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear " + packageName);
            Log.i(TAG, "App Data Cleared !!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void LoadProfile() {

        if (fUser.getDisplayName() != null) {
            UserName = fUser.getDisplayName();
            UserNameET.setText(UserName);
        }
        if (fUser.getEmail() != null) {
            EmailID = fUser.getEmail();
            EmailIDET.setText(EmailID);
        }
        if (fUser.getPhotoUrl() != null) {
            PhotoURL = fUser.getPhotoUrl().toString();
        }
        setProfileImage();

        PhoneNoTV.setText(fUser.getPhoneNumber());


// Read from the database
        UserBioDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot != null) {
                    if (dataSnapshot.getValue(String.class) != null) {

                        String value = dataSnapshot.getValue(String.class);
                        Log.d(TAG, "Value is: " + value);
                        UserBioET.setText(value);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    final static String FILE_PROFILE_PIC="ProfilePicFile.jpg";
    void setProfileImage() {
        Log.d(TAG, "setProfileImage: Profile Photo URL: " + PhotoURL);
        Glide.get(this).clearMemory();

  /*      Glide.with(this).load(PhotoURL)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_baseline_account_circle_30)
                .into(ProfileIMG);
*/
        Glide.with(this).asBitmap().load(PhotoURL) .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                try {
                    Log.d(TAG, "ProfilePic onResourceReady: ");
                    File file = new File(getFilesDir(), FILE_PROFILE_PIC);
                    FileOutputStream out = new FileOutputStream(file);
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();

                    ProfileIMG.setImageBitmap(resource);
                    Log.d(TAG, "ProfilePic onResourceReady: SAVED");

                } catch( Exception e) {
                    Log.d(TAG, "Profile Pic ERROR #45643 "+e);
                }



            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });


        if(ProfilePicFile.exists()){
            Log.d(TAG, "setProfileImage: ProfilePicFile.exists()");
            Bitmap myBitmap = BitmapFactory.decodeFile(ProfilePicFile.getAbsolutePath());
            ProfileIMG.setImageBitmap(myBitmap);
            Log.d(TAG, "setProfileImage: ProfilePicFile.exists() APPLIED");

        }
    }

    @Override
    protected void onDestroy() {
        //SaveProfile();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void SaveProfile(View v) {
        if (UserNameET.getText().length() != 0) {
            UserName = UserNameET.getText().toString();
        } else {
            //UserNameET.setError("What's your name ?");
        }
        if (EmailIDET.getText().length() != 0) {
            EmailID = EmailIDET.getText().toString();

        } else {
            //  EmailIDET.setError("What's your Email ID ?");
        }
        UserBio = UserBioET.getText().toString();
        UserBioDB.setValue(UserBio);

        savingView.setVisibility(View.VISIBLE);

        //   Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();


        PhotoURL = "https://storage.googleapis.com/" + firebase_project_id + ".appspot.com/indra/users/" + fUser.getUid() + "/app_user_photo.jpg";

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(" ")
                .setPhotoUri(Uri.parse(PhotoURL))
                .build();

        if (UserName != null) {
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(UserName)
                    .setPhotoUri(Uri.parse(PhotoURL))
                    .build();
        }


        fUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");

                            Toast.makeText(ProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            savingView.setVisibility(View.GONE);

                            /*fUser.updateEmail(EmailID)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User email address updated.");

                                                sendVerificationEmail();


                                            }
                                        }
                                    });*/


                        }
                    }

                });


    }

    void sendVerificationEmail() {
        if (!fUser.isEmailVerified()) {
            fUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Verification Email sent.");
                                //Toast.makeText(ProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                                savingView.setVisibility(View.GONE);
                            }
                        }
                    });
        }

    }

    public void chooseProfilePhoto(View v) {
        easyImage.openGallery(this);
    }

    void onPhotosReturned(MediaFile[] imageFiles) {

        File imgFile = imageFiles[0].getFile();
        Log.d(TAG, "onPhotosReturned: " + imgFile);

        File imgFileDest = new File(getCacheDir(), "temp_photo_" + imgFile.getName());

        UCrop.of(Uri.fromFile(imgFile), Uri.fromFile(imgFileDest))
                .withAspectRatio(1, 1)
                //.withMaxResultSize(maxWidth, maxHeight)
                .start(this);
    }

    public void compressImage(Uri img) {
        File imgFinal;
        try {
            imgFinal = new Compressor(this)
                    //.setMaxWidth(500)
                    .setMaxHeight(500)
                    .setQuality(50)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToFile(new File(img.getPath()));

            Log.d(TAG, "compressImage: imgFinal: " + imgFinal);
            UploadImage(imgFinal);
        } catch (Exception e) {
            Log.e(TAG, "compressImage: ", e);
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Log.d(TAG, "onActivityResult: " + resultUri);
            compressImage(resultUri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Error: Try again", Toast.LENGTH_SHORT).show();
            final Throwable cropError = UCrop.getError(data);
        }

    }

    void UploadImage(File img) {
        savingView.setVisibility(View.VISIBLE);
        // StorageReference imagesRef = storageRef.child("indra/users/"+fUser.getUid()+"/app_user_photo.jpg");
        Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());


        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCacheControl("no-cache")
                .setCustomMetadata("app", "indra")
                .setCustomMetadata("file_type", "profile_photo")
                .setCustomMetadata("owner_id", mAuth.getCurrentUser().getUid())
                .setCustomMetadata("owner_phone", mAuth.getCurrentUser().getPhoneNumber())
                .setCustomMetadata("epoch", String.valueOf(System.currentTimeMillis()))
                .build();

        Uri file = Uri.fromFile(img);
        StorageReference ProfilePhotoRef = storageRef.child("indra/users/" + fUser.getUid() + "/app_user_photo.jpg");
        ProfilePhotoRef.delete();
        UploadTask uploadTask = ProfilePhotoRef.putFile(file, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "onFailure: Profile Photo Upload ");
                Toast.makeText(ProfileActivity.this, "Error: try again", Toast.LENGTH_SHORT).show();
                savingView.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG, "onSuccess: Profile Photo Upload Success Sync ");
                Toast.makeText(ProfileActivity.this, "Profile Photo Updated", Toast.LENGTH_SHORT).show();
                // LoadProfile();
//                Task<Uri> DownloadURL=ProfilePhotoRef.getDownloadUrl();
                savingView.setVisibility(View.GONE);
                ProfileIMG.setImageBitmap(myBitmap);

                try {
                   // File file = new File(getFilesDir(), FILE_PROFILE_PIC);
                    FileOutputStream out = new FileOutputStream(ProfilePicFile);
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Log.d(TAG, "onSuccess: Profile pic cache saved");
                } catch( Exception e) {
                    Log.d(TAG, "ERROR #45675 "+e);
                }


            }
        });

        PhotoURL = "https://storage.googleapis.com/" + firebase_project_id + ".appspot.com/indra/users/" + fUser.getUid() + "/app_user_photo.jpg";
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                //.setDisplayName(UserName)
                .setPhotoUri(Uri.parse(PhotoURL))
                .build();

        fUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            // Toast.makeText(ProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            //  savingView.setVisibility(View.GONE);

                        }
                    }

                });

    }

}

/*
 * https://storage.googleapis.com/ephrinelab.appspot.com/indra/users/3bfZNZJBVwaSyU0qhukFurhgfBo2/app_user_photo.jpg
 * */