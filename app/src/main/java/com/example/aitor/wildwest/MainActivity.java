package com.example.aitor.wildwest;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity {

    private VistaJuego vj;
    private boolean soundEnabled = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wildwest_layout);
        vj = (VistaJuego) findViewById(R.id.mole);
    }

}
