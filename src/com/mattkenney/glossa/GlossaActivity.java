package com.mattkenney.glossa;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GlossaActivity
    extends ListActivity
    implements AdapterView.OnItemClickListener
{
    public static final String PREFS_NAME = "Glossa";

    private final DataSetObserver observer = new DataSetObserver()
    {
        private boolean hasFooter = true;

        @Override
        public void onChanged()
        {
            if (adapter.getCount() != 0)
            {
                hasFooter = false;
                getListView().removeFooterView(footer);
            }
            else if (!hasFooter)
            {
                hasFooter = true;
                getListView().addFooterView(footer);
            }
        }
    };

    private ArrayAdapter<String> adapter = null;
    private TextView header = null;
    private ProgressBar footer = null;
    private View attribution = null;

    public GlossaActivity()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // create an array of Strings, that will be put to our ListActivity
        ListView list = getListView();
        list.setOnItemClickListener(this);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1);
        header = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        header.setTextColor(0xff0099cc);
        footer = new ProgressBar(this, null, android.R.attr.progressBarStyle);
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        attribution = inflater.inflate(R.layout.attribution, null);
        
        list.addHeaderView(header);
        list.addFooterView(footer);
        list.addFooterView(attribution);
        setListAdapter(adapter);
        adapter.registerDataSetObserver(observer);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        adapter.clear();
        adapter.notifyDataSetChanged();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type))
        {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String from = prefs.getString("from", "fr");
            String to = prefs.getString("to", "en");

            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            header.setText(sharedText);
            new TranslateTask(adapter).execute(sharedText, from, to);
        }
    }
}
