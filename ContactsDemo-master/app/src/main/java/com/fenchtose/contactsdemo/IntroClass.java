package com.fenchtose.contactsdemo;

/**
 * Created by aseem on 2/19/16.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class IntroClass extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Button proceedButton;
    private CheckBox agreeCheckBox, disagreeCheckBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
        setContentView(R.layout.fragment_intro);
        proceedButton = (Button) findViewById(R.id.proceed_button);
        agreeCheckBox = (CheckBox) findViewById(R.id.agree_checkBox);
        disagreeCheckBox = (CheckBox) findViewById(R.id.disagree_checkBox);

        proceedButton.setOnClickListener(this);
        agreeCheckBox.setOnCheckedChangeListener(this);
        disagreeCheckBox.setOnCheckedChangeListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.proceed_button:
                proceedAction();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.agree_checkBox) {
            disagreeCheckBox.setChecked(!isChecked);
        } else {
            agreeCheckBox.setChecked(!isChecked);
        }
    }

    private void proceedAction() {
        if (disagreeCheckBox.isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Disclaimer");
            builder.setMessage("Please agree to the disclaimer to use this app!");
            builder.show();
        } else {
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
        }
    }
}

