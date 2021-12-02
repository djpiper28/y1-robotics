package uk.co.rhul.r14.letamagotchijos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

interface StringConsumer {
    void consume(String str);
}

public class NewRobotScreen extends AppCompatActivity {

    private final static String[] randomNames = {"Allan", "Barry", "Greg", "Joe", "Francis", "Alberto",
            "James", "John", "Daniel", "Obama", "Mark", "Steve", "Alex", "Harry", "Erin", "Edward",
            "Kate", "Dave", "Margret", "Boris", "Grant", "Neil", "Pier", "Bob"};
    private static StringConsumer onNameSet;
    private static boolean hasCancel;
    //Remind me to grab random names from  a random forum and dump them here

    public static void setVars(StringConsumer onNameSet, boolean hasCancel) {
        NewRobotScreen.onNameSet = onNameSet;
        NewRobotScreen.hasCancel = hasCancel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bot);

        if (savedInstanceState == null) {
            EditText name = findViewById(R.id.tamatgotchi_name);
            name.setText(randomNames[(new Random()).nextInt(randomNames.length)]);

            Button connect = findViewById(R.id.connect_to_ev3);
            connect.setOnClickListener(click -> {
                super.onBackPressed();
                NewRobotScreen.onNameSet.consume(name.getText().toString());
            });
        }

    }

    @Override
    public void onBackPressed() {
        if (hasCancel) super.onBackPressed();
    }
}