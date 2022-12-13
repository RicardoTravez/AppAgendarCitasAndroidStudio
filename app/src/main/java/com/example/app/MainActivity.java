package com.example.app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.app.backend.ConexionSQLiteHelper;
import com.example.app.backend.utils.Utilidades;

/**
 * Clase para iniciar sesión, si el usuario no tiene cuenta, se puede registrar
 */

public class MainActivity extends AppCompatActivity {
    Button git;
    // Campos
    public EditText editTextEmail, editTextPassword;

    AwesomeValidation awesomeValidation;
    com.example.app.backend.ConexionSQLiteHelper conn;
    SQLiteDatabase db;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        git = findViewById(R.id.git);

        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goLink("https://github.com/RicardoTravez");
            }
        });

        // Obteniendo los campos
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        awesomeValidation = new AwesomeValidation(ValidationStyle.UNDERLABEL);
        awesomeValidation.setContext(this);
        awesomeValidation.setUnderlabelColor(ContextCompat.getColor(this, android.R.color.holo_red_light));

        // Validación correo
        awesomeValidation.addValidation(this, R.id.editTextEmail, Patterns.EMAIL_ADDRESS, R.string.error_email);

        // Validación contraseña
        awesomeValidation.addValidation(this, R.id.editTextPassword, ".{6,}", R.string.error_password);

        conn = new com.example.app.backend.ConexionSQLiteHelper(getApplicationContext(),"bd_usuarios",null,1);
    }

    private void goLink(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW,uri));
    }

    // Métodos públicos
    // Método que valida el formulario
    public void buttonLogin(View view) {
        String email, password;
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        if (awesomeValidation.validate()) if (checkEmail(email)) {
            if (checkEmailPassword(email, password)) {
                SharedPreferences sharedPref = this.getSharedPreferences("correo_electronico", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.email), email);
                editor.apply();

                Intent intent = new Intent(this, com.example.app.PrincipalMenu.class);
                startActivity(intent);
            }
        }
    }

    public void buttonSignUp(View view) {
        Intent intent = new Intent(this, com.example.app.SignUp.class);
        startActivity(intent);
    }

    public Boolean checkEmail(String email) {
        db = conn.getReadableDatabase();
        String[] parametros = { email };
        String[] campos = { com.example.app.backend.utils.Utilidades.CAMPO_NOMBRE };

        try {
            // Select correo electrónico from usuario where correo electrónico =?
            Cursor cursor = db.query(com.example.app.backend.utils.Utilidades.TABLA_USUARIO,
                    campos,
                    com.example.app.backend.utils.Utilidades.CAMPO_ID_EMAIL + " = ? ",
                    parametros,
                    null,
                    null,
                    null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                SharedPreferences sharedPref = this.getSharedPreferences("nombre", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.first_name), cursor.getString(0));
                editor.apply();

                cursor.close();
                return true;
            } else {

                editTextPassword.setText("");

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog);
                builder.setTitle(R.string.email_not_found);
                builder.setMessage(email + " no coincide con ninguna cuenta existente. Puedes crear una cuenta para acceder.");
                builder.setNegativeButton(R.string.try_again, (dialog, which) -> {

                });
                builder.setPositiveButton(R.string.sign_up, (dialog, which) -> {
                    // Hacer cosas aqui al hacer clic en el boton de aceptar
                    Intent intent = new Intent(this, com.example.app.SignUp.class);
                    startActivity(intent);
                });
                builder.show();
            }
        } catch (Exception e) {

            Toast.makeText(this, e + "", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public Boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase db = conn.getReadableDatabase();

        try {
            // Select correo electrónico from usuario where correo electrónico =?
            Cursor cursor = db.rawQuery("Select " + Utilidades.CAMPO_ID_EMAIL + ", " + com.example.app.backend.utils.Utilidades.CAMPO_PASSWORD +
                            " from " + com.example.app.backend.utils.Utilidades.TABLA_USUARIO
                            + " where " + com.example.app.backend.utils.Utilidades.CAMPO_ID_EMAIL + " = ?  and " + com.example.app.backend.utils.Utilidades.CAMPO_PASSWORD + "= ?",
                    new String[] { email, password });

            if (cursor.getCount() > 0) {
                return true;

            } else {

                editTextPassword.setText("");
                cursor.close();

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog);
                builder.setTitle(R.string.email_not_match);
                builder.setMessage("La contraseña no coincide con la cuenta registrada.");
                builder.setNegativeButton(R.string.try_again, (dialog, which) -> {

                });
                builder.show();
            }
        } catch (Exception e) {

            Toast.makeText(this, e + "", Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}