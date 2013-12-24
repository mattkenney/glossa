package com.mattkenney.glossa;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SetupActivity
    extends Activity
    implements AdapterView.OnItemSelectedListener
{
    public SetupActivity()
    {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                 R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SharedPreferences prefs = getSharedPreferences(GlossaActivity.PREFS_NAME, MODE_PRIVATE);
        String from = "[" + prefs.getString("from", "fr") +"]";
        String to = "[" + prefs.getString("to", "en") + "]";

        Spinner spinner = (Spinner) findViewById(R.id.from);
        spinner.setTag("from");
        spinner.setAdapter(adapter);
        for (int i = 0; i < adapter.getCount(); i++)
        {
            if (adapter.getItem(i).toString().endsWith(from))
            {
                spinner.setSelection(i);
                break;
            }
        }
        spinner.setOnItemSelectedListener(this);

        spinner = (Spinner) findViewById(R.id.to);
        spinner.setTag("to");
        spinner.setAdapter(adapter);
        for (int i = 0; i < adapter.getCount(); i++)
        {
            if (adapter.getItem(i).toString().endsWith(to))
            {
                spinner.setSelection(i);
                break;
            }
        }
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        SharedPreferences prefs = getSharedPreferences(GlossaActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String[] parts = String.valueOf(parent.getItemAtPosition(position)).split("\\[|\\]");
        if (parts.length > 1)
        {
            editor.putString(String.valueOf(parent.getTag()), parts[1]);
            editor.commit();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        SharedPreferences prefs = getSharedPreferences(GlossaActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(String.valueOf(parent.getTag()));
        editor.commit();
    }
}
