package devesh.ephrine.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import devesh.ephrine.MainActivity;
import devesh.ephrine.R;
import devesh.ephrine.util.AppAnalytics;
import devesh.common.utils.CachePref;


public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private static final String KEY_VERIFICATION_ID = "key_verification_id";

    View mView;

    LinearLayout LoginLL;
    LinearLayout VerifyOTPLL;
    EditText editTextPhone;
    EditText editTextOTP;
    TextView LoginStatusText;
    RelativeLayout RLStatus;
    String mPhoneNumber;
    TextView mPhoneNumberTx;
    TextView ResendCodeTx;
    TextView PPText;
    CountryCodePicker ccp;


    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAnalytics mFirebaseAnalytics;

    AppAnalytics appAnalytics;
CachePref cachePref;
    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mView = view;

        LoginLL = view.findViewById(R.id.LoginLL);
        VerifyOTPLL = view.findViewById(R.id.VerifyOTPLL);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextOTP = view.findViewById(R.id.editTextOTP);

        LoginStatusText = view.findViewById(R.id.textViewLoginStatusText);
        RLStatus = view.findViewById(R.id.RLStatus);

        mPhoneNumberTx = view.findViewById(R.id.textViewmPhoneNumber);

        ResendCodeTx = view.findViewById(R.id.textViewResendCode1);

        PPText = view.findViewById(R.id.PrivacyPolicyTxView);
        ccp = view.findViewById(R.id.ccp);
        Button ButtonOtpVerify = view.findViewById(R.id.buttonOtpVerify);
        Button ButtonSignInPhone = view.findViewById(R.id.buttonSignInPhone);


        cachePref=new CachePref(getActivity());
        appAnalytics=new AppAnalytics(getActivity().getApplication(),getActivity());

        if (mVerificationId == null && savedInstanceState != null) {
mVerificationId= savedInstanceState.getString(KEY_VERIFICATION_ID);

        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());


        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();


        ButtonSignInPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(getActivity());
                String ct = ccp.getSelectedCountryCodeWithPlus();
                mPhoneNumber = editTextPhone.getText().toString();

                cachePref.setString(mView.getContext().getString(R.string.Pref_Country_Code_with_Plus),ct );
                startPhoneNumberVerification(ct + mPhoneNumber);
                RLStatusView("Verifying....");

                Map<String,String> adata=new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Sign-in Button Click");
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

            }
        });


        ButtonOtpVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(getActivity());
                RLStatusView("Verifying....");
                String OTP = editTextOTP.getText().toString();

                verifyPhoneNumberWithCode(mVerificationId, OTP);

                Map<String,String> adata=new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"OTP Verify Button Click");
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);

            }
        });

        PPText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openPrivacyPolicy();
                Map<String,String> adata=new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Read Privacy Policy");
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata);


            }
        });

        ShowPhoneNumberLL();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // getActivity()  callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);

                Map<String,String> adata=new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin: onVerificationCompleted");
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),adata);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // getActivity()  callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
String analyticString="";
                String analyticMsgString="";
                String analyticUserShowedMSGString="";
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    ErrorAlert("Error", "Invalid request");
                    analyticUserShowedMSGString="Invalid request";
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    ErrorAlert("Error", "The SMS quota for the Indra App has been exceeded");
                    analyticUserShowedMSGString="The SMS quota for the Indra App has been exceeded";
                } else {
                    ErrorAlert("Error", e.getMessage());
                    analyticUserShowedMSGString="RAW Message";
                }
                analyticString=e.toString();
                analyticMsgString=e.getMessage();

                Map<String,String> adata=new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin: onVerificationFailed");
                adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin Error: "+analyticString);
                adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin Error: "+analyticMsgString);
                adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin Error: "+analyticUserShowedMSGString);
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),adata);
                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                VerifyOTPLL();

                Map<String,String> adata=new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin: onCodeSent OTP");
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),adata);

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                //Toast.makeText(getActivity(), "TimeOut", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCodeAutoRetrievalTimeOut: OTP Time out \n" + s);
              try {
                  ShowResendButton();
                  Map<String,String> adata=new HashMap<>();
                  adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin: onCodeAutoRetrievalTimeOut OTP");
                  appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),adata);

              }catch (Exception e){
                  Log.e(TAG, "onCodeAutoRetrievalTimeOut: "+e );
              }

                super.onCodeAutoRetrievalTimeOut(s);
            }
        };

        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),"Login Fragment Screen");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_APP_FLOW),adata);


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_VERIFICATION_ID,mVerificationId);
        Map<String,String> adata=new HashMap<>();
        adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin: onSaveInstanceState");
        appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),adata);

    }


    @Override
    public void onStart() {
        super.onStart();


    }

    void ShowPhoneNumberLL() {
        VerifyOTPLL.setVisibility(View.GONE);
        LoginLL.setVisibility(View.VISIBLE);
        RLStatus.setVisibility(View.GONE);
    }

    void VerifyOTPLL() {
        VerifyOTPLL.setVisibility(View.VISIBLE);
        LoginLL.setVisibility(View.GONE);
        RLStatus.setVisibility(View.GONE);
        ResendCodeTx.setVisibility(View.GONE);
        mPhoneNumberTx.setText("+91 " + mPhoneNumber);

        editTextOTP.setText("");

    }

    void LoginComplete(FirebaseUser user) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).LoginComplete(user);
        }
    }

    void RLStatusView(String status_text) {
        VerifyOTPLL.setVisibility(View.GONE);
        LoginLL.setVisibility(View.GONE);
        RLStatus.setVisibility(View.VISIBLE);
        LoginStatusText.setText(status_text);
    }

    void ShowResendButton() {
        ResendCodeTx.setVisibility(View.VISIBLE);
        ResendCodeTx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RLStatusView("Resending OTP Code...");
                resendVerificationCode("+91" + mPhoneNumber, mResendToken);

                Map<String,String> adata=new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),"Sigin: Reset OTP");
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_APP_SYSTEM_EVENTS),adata);

                Map<String,String> adata2=new HashMap<>();
                adata.put(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),"Sigin: Reset OTP");
                appAnalytics.logEvent(getString(R.string.ANALYTICS_EVENT_NAME_USER_FLOW),adata2);

            }
        });
    }

    void ErrorAlert(String title, String message) {
        ShowPhoneNumberLL();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message)
                .setTitle(title)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void startPhoneNumberVerification(String phoneNumber) {
        Log.d(TAG, "startPhoneNumberVerification: ");

        // [START start_phone_auth]
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        // [END start_phone_auth]
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        Log.d(TAG, "verifyPhoneNumberWithCode: ");
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        Log.d(TAG, "resendVerificationCode: ");
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                            LoginComplete(user);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                ErrorAlert("Unable to Login", "The verification code entered was invalid");
                            } else {
                            }
                        }
                    }
                });

    }


    void openPrivacyPolicy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(
                "https://www.ephrine.in/privacy-policy.html"));
        startActivity(intent);
    }

}