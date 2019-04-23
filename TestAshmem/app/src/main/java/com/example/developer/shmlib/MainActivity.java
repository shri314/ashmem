package com.example.developer.shmlib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    EditText edpos, edval;
    Button bn;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShmLib.OpenSharedMem("sh1", 1000, true);

        edpos = findViewById(R.id.ed2);
        edval = findViewById(R.id.ed);

        tv = findViewById(R.id.tv);
        bn = findViewById(R.id.btnSet);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShmLib.setValue("sh1", Integer.parseInt(edpos.getText().toString()), Integer.parseInt(edval.getText().toString()));
            }
        });
        Button bget = findViewById(R.id.btnGet);
        bget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int res = ShmLib.getValue("sh1", Integer.parseInt(edpos.getText().toString()));
                tv.setText("res:" + res);

            }
        });


    }

}
