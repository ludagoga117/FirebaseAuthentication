package perez.paula.firebaseauthentication;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final int LOGIN_CON_GOOGLE = 1;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    private SignInButton btnSignInGoogle;
    private LoginButton btnSignInFacebook;

    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        FacebookSdk.sdkInitialize( getApplicationContext() );
        callbackManager = CallbackManager.Factory.create();

        /*// Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "perez.paula.firebaseauthentication",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/

        btnSignInGoogle = (SignInButton) findViewById(R.id.btnSignInGoogle);
        btnSignInFacebook = (LoginButton) findViewById(R.id.btnSignInFacebook);

        inicializar();

    }

    private void inicializar(){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if( firebaseUser != null ){
                    Log.d("FirebaseUser", "Usuario logeado"+firebaseUser.getDisplayName() );
                    Log.d("FirebaseUser", "Usuario logeado"+firebaseUser.getEmail() );
                }else{
                    Log.d("FirebaseUser", "El usuario ha cerrado sesión" );
                }
            }
        };
        // Inicializar cuenta de google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken( getString(R.string.default_web_client_id) )
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder( LoginActivity.this )
        .enableAutoManage(LoginActivity.this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = Auth.GoogleSignInApi.getSignInIntent( googleApiClient );
                startActivityForResult( i, LOGIN_CON_GOOGLE );
            }
        });

        btnSignInFacebook.setReadPermissions("email","public_profile");
        btnSignInFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("LoginFacebook", "Exito");
                signInFacebook( loginResult.getAccessToken() );
            }

            @Override
            public void onCancel() {
                Log.d("LoginFacebook", "Cancelado");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("LoginFacebook", "Error");
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == LOGIN_CON_GOOGLE ){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(data);
            signInGoogle(googleSignInResult);
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void signInGoogle( GoogleSignInResult googleSignInResult ){
        if( googleSignInResult.isSuccess() ){
            AuthCredential authCredential = GoogleAuthProvider.getCredential(
                    googleSignInResult.getSignInAccount().getIdToken(), null
            );
            firebaseAuth.signInWithCredential( authCredential ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    goOtherActivity();
                }
            });
        }else{
            Toast.makeText(LoginActivity.this, "Autentificacion con google " +
                    "fallida", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void signInFacebook(AccessToken accessToken){
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential( authCredential ).addOnCompleteListener(LoginActivity
                .this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if( task.isSuccessful() ){
                    goOtherActivity();
                }else{
                    Toast.makeText(LoginActivity.this, "Autentificacion con facebook " +
                            "fallida", Toast
                            .LENGTH_LONG).show();
                }
            }
        });
    }

    private void goOtherActivity(){
        Intent i = new Intent ( LoginActivity.this, MainActivity.class );
        startActivity(i);
        finish();
    }
}
