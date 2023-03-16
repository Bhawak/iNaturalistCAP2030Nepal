package org.inaturalist.android.capnepal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.livefront.bridge.Bridge;

import org.inaturalist.android.BaseFragmentActivity;
import org.inaturalist.android.INaturalistApp;
import org.inaturalist.android.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QuestActivity extends BaseFragmentActivity {
    private INaturalistApp mApp;
    private DatabaseReference questDatabase;
    private List<Quest> questList= new ArrayList<>();
    QuestAdapter questAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bridge.restoreInstanceState(this, savedInstanceState);
        mApp = (INaturalistApp)getApplication();
        mApp.applyLocaleSettings(getBaseContext());
        setContentView(R.layout.quest_activity);
        RecyclerView rvQuest = (RecyclerView) findViewById(R.id.quest_listview);
        questAdapter = new QuestAdapter(questList);
        rvQuest.setAdapter(questAdapter);
        rvQuest.setLayoutManager(new LinearLayoutManager(this));
        onDrawerCreate(savedInstanceState);
        basicReadWrite(questAdapter);
    }
    public void basicReadWrite(QuestAdapter questAdapter){
        questDatabase = çDatabase.getInstance("https://cap2030-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
//        Query mQuery = questDatabase.child("CAP2030");
        questDatabase.addValueEventListener(new ValueEventListener() {
            boolean update = true;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if(postSnapshot.hasChildren()){
                            for (DataSnapshot childSnapshot: postSnapshot.getChildren()){
                                if(childSnapshot.hasChildren()){
                                    questList.clear();
                                    for (DataSnapshot grandchildSnapshot: childSnapshot.getChildren()
                                         ) {
                                        Quest mQuest = grandchildSnapshot.getValue(Quest.class);
                                        questList.add(mQuest);
                                    }
                                }
//
                            }
                            Comparator<Quest> defaultComparator = new Comparator<Quest>() {
                                @Override
                                public int compare(Quest o1, Quest o2) {
                                    return o2.getId().compareTo(o1.getId());
                                }
                            };
                            Collections.sort(questList, defaultComparator);
                            questAdapter.notifyDataSetChanged();
                            SharedPreferences prefs = getSharedPreferences("iNaturalistPreferences", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("unread_quests", false);
                            editor.apply();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
