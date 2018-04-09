package perez.paula.firebaseauthentication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });

        inicializar();
    }

    private void cerrarSesion(){
        firebaseAuth.signOut();
        if( Auth.GoogleSignInApi != null ){
            Auth.GoogleSignInApi.signOut( googleApiClient ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if( status.isSuccess() ){
                        goToLogin();
                    }else{
                        Toast.makeText(MainActivity.this, "Error cerrando sesión de google",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        if(LoginManager.getInstance() != null ){
            LoginManager.getInstance().logOut();
        }

    }

    private void goToLogin(){
        Intent i = new Intent( MainActivity.this, LoginActivity.class );
        startActivity(i);
        finish();
    }

    private void inicializar(){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if( firebaseUser != null ){
                    Log.d("FirebaseUser", "Correo: "+firebaseUser.getEmail() );
                }else{
                    Log.d("FirebaseUser", "El usuario ha cerrado sesión" );
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener( authStateListener );
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener( authStateListener );
    }
}
