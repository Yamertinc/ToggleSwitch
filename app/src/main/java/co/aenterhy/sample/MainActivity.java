package co.aenterhy.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import co.aenterhy.toggleswitch.ToggleSwitchButton;

public class MainActivity extends AppCompatActivity {

    private ToggleSwitchButton toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggle = (ToggleSwitchButton) findViewById(R.id.toggle);
        toggle.setOnTriggerListener(new ToggleSwitchButton.OnTriggerListener() {
            @Override
            public void toggledUp() {
                Toast.makeText(MainActivity.this, "Video", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void toggledDown() {
                Toast.makeText(MainActivity.this, "Audio", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
