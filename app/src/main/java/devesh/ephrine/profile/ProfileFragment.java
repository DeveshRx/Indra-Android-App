package devesh.ephrine.profile;


/*
public class ProfileFragment extends Fragment {
    EasyImage easyImage;

    FirebaseAuth mAuth;
    FirebaseUser fUser;

    CircleImageView ProfileIMG;

    TextInputEditText UserNameET;
    TextInputEditText EmailIDET;

    TextView PhoneNoTV;

    String UserName = null;
    String EmailID = null;
    String PhotoURL = null;
    String TAG = "ProfileActivity: ";
    FirebaseStorage storage;
    StorageReference storageRef;
    String firebase_project_id;

    public ProfileFragment() {
        super(R.layout.activity_profile);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        //int someInt = requireArguments().getInt("some_int");

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        firebase_project_id= FirebaseApp.getInstance().getOptions().getProjectId();

        PhotoURL="https://storage.googleapis.com/"+firebase_project_id+".appspot.com/nue/users/"+fUser.getUid()+"/app_user_photo.jpg";

        UserNameET = view.findViewById(R.id.UserNameET);
        EmailIDET = view.findViewById(R.id.UserEmailET);
        ProfileIMG = view.findViewById(R.id.ProfileImageView);
        PhoneNoTV = view.findViewById(R.id.PhoneNoTextView);

        easyImage = new EasyImage.Builder(getActivity())
                .setChooserTitle("Pick Photo")
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setCopyImagesToPublicGalleryFolder(false)
                .setFolderName("Nue")
                .allowMultiple(false)
                .build();
        LoadProfile();

    }
    void LoadProfile(){

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

    }

    void setProfileImage() {
        Log.d(TAG, "setProfileImage: Profile Photo URL: "+PhotoURL);
        Glide.get(this).clearMemory();

        Glide.with(this).load(PhotoURL)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ProfileIMG);
    }


    public void SaveProfile(View v) {
        if (UserNameET.getText().length() != 0) {
            if (EmailIDET.getText().length() != 0) {

                Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
                UserName = UserNameET.getText().toString();

                EmailID = EmailIDET.getText().toString();

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(UserName)
                        .setPhotoUri(Uri.parse(PhotoURL))
                        .build();

                fUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                    Toast.makeText(ProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                fUser.updateEmail(EmailID)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User email address updated.");
                                    sendVerificationEmail();
                                }
                            }
                        });


            } else {
                EmailIDET.setError("What's your Email ID ?");
            }
        } else {
            UserNameET.setError("What's your name ?");
        }


    }

    void sendVerificationEmail() {
        if (!fUser.isEmailVerified()) {
            fUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Verification Email sent.");
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

            Log.d(TAG, "compressImage: imgFinal: "+imgFinal);
            UploadImage(imgFinal);
        } catch (Exception e) {
            Log.e(TAG, "compressImage: ", e);
        }


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    void UploadImage(File img){
        // StorageReference imagesRef = storageRef.child("nue/users/"+fUser.getUid()+"/app_user_photo.jpg");
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCacheControl("no-cache")
                .setCustomMetadata("app", "nue")
                .setCustomMetadata("file_type", "profile_photo")
                .setCustomMetadata("owner_id", mAuth.getCurrentUser().getUid())
                .setCustomMetadata("owner_phone", mAuth.getCurrentUser().getPhoneNumber())
                .setCustomMetadata("epoch", String.valueOf(System.currentTimeMillis()))
                .build();

        Uri file = Uri.fromFile(img);
        StorageReference ProfilePhotoRef = storageRef.child("nue/users/" + fUser.getUid() + "/app_user_photo.jpg");
        ProfilePhotoRef.delete();
        UploadTask uploadTask = ProfilePhotoRef.putFile(file, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "onFailure: Profile Photo Upload ");
                Toast.makeText(ProfileActivity.this, "Error: try again", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG, "onSuccess: Profile Photo Upload Success Sync ");
                Toast.makeText(ProfileActivity.this, "Profile Photo Updated", Toast.LENGTH_SHORT).show();
                LoadProfile();
//                Task<Uri> DownloadURL=ProfilePhotoRef.getDownloadUrl();

            }
        });


    }

}
*/