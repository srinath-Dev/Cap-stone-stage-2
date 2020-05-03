package com.aborteddevelopers.tata;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aborteddevelopers.tata.Common.Common;
import com.aborteddevelopers.tata.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnRegister;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    RelativeLayout rootLayout;
    TextView txt_forget_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Paper.init(this);
        
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_driver_tb1);

        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        txt_forget_pwd = (TextView)findViewById(R.id.txt_rider_app);
        txt_forget_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialogForgotPwd();
                return false;
            }
        });
        
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogInDialog();
            }
        });

        String user = Paper.book().read(Common.user_field);
        String pwd = Paper.book().read(Common.pwd_field);
        if (user !=null && pwd  != null)
        {
            if (!TextUtils.isEmpty(user) &&
                       !TextUtils.isEmpty(pwd))

            {
                autoLogin(user,pwd);

            }
        }

    }

    private void printKeyHash() {

        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.aborteddevelopers.tata",
                    PackageManager.GET_SIGNATURES);

            for (Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KEYHASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void autoLogin(String user, String pwd) {



        final android.app.AlertDialog waitingDialogs = new SpotsDialog.Builder().setContext(MainActivity.this).build();
        waitingDialogs.show();

        auth.signInWithEmailAndPassword(user,pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        waitingDialogs.dismiss();

                        FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                        startActivity(new Intent(MainActivity.this, DriverHome.class));
                                        waitingDialogs.dismiss();
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });





                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                waitingDialogs.dismiss();

                Snackbar.make(rootLayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                btnSignIn.setEnabled(true);
            }
        });


    }



    private void showDialogForgotPwd() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("FORGOT PASSWORD");
        alertDialog.setMessage("Please enter your email address");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forget_pwd,null);

        final MaterialEditText edtEmail = (MaterialEditText)forgot_pwd_layout.findViewById(R.id.edtForgetEmail);
        alertDialog.setView(forgot_pwd_layout);

        //Set Button

        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                dialogInterface.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout,"Reset password link has been sent",Snackbar.LENGTH_LONG).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLayout,"Something Wrong Try again !"+e.getMessage(),Snackbar.LENGTH_LONG).show();



                    }
                });

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

            }
        });

        alertDialog.show();
    }

    private void showLogInDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to SIGN IN");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                btnSignIn.setEnabled(false);


                if (TextUtils.isEmpty(edtEmail.getText().toString())) {

                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;

                }

                if (edtPassword.getText().toString().length() < 6) {

                    Snackbar.make(rootLayout, "Password too short !!", Snackbar.LENGTH_SHORT).show();
                    return;

                }


                final android.app.AlertDialog waitingDialogs = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                waitingDialogs.show();

                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                waitingDialogs.dismiss();

                                FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                Paper.book().write(Common.user_field,edtEmail.getText().toString());
                                                Paper.book().write(Common.pwd_field,edtPassword.getText().toString());
                                                Common.currentUser = dataSnapshot.getValue(User.class);
                                                startActivity(new Intent(MainActivity.this, DriverHome.class));
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });





                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        waitingDialogs.dismiss();

                        Snackbar.make(rootLayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        btnSignIn.setEnabled(true);
                    }
                });


            }

        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });


        dialog.show();



    }

    private void showRegisterDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                if (TextUtils.isEmpty(edtEmail.getText().toString()))
                {

                    Snackbar.make(rootLayout,"Please enter email address",Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if (TextUtils.isEmpty(edtName.getText().toString()))
                {

                    Snackbar.make(rootLayout,"Please enter Name",Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if (TextUtils.isEmpty(edtPhone.getText().toString()))
                {

                    Snackbar.make(rootLayout,"Please enter Phone number",Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if (edtPassword.getText().toString().length()<6)
                {

                    Snackbar.make(rootLayout,"Password too short !!",Snackbar.LENGTH_SHORT).show();
                    return;

                }

                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setPhone(edtPhone.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setAvatarUrl("");
                                user.setRates("0");
                                user.setCarType("TATA X");


                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(  rootLayout,"Register Successful !!",Snackbar.LENGTH_SHORT).show();



                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();

                    }
                });


            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();

            }
        });

        dialog.show();

    }
}
