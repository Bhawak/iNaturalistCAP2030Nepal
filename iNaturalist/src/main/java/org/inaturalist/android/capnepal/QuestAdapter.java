package org.inaturalist.android.capnepal;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.inaturalist.android.R;
import org.inaturalist.android.TutorialActivity;

import java.io.Serializable;
import java.util.List;

import timber.log.Timber;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {
    private List<Quest> questList;

    public QuestAdapter(List<Quest> quests){
        questList = quests;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View questView = inflater.inflate(R.layout.quest_element, parent, false);
        ViewHolder viewHolder = new ViewHolder(questView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Quest quest = questList.get(position);
        CardView cardView = holder.cardView;
        TextView lNameTV = holder.lNameTV;
        TextView sNameTV = holder.sNameTV;
        TextView descTV = holder.descriptionTV;
        TextView validTV = holder.dueDateTV;
        ImageView imageView = holder.speciesIV;

        lNameTV.setText(quest.getLocalName());
        sNameTV.setText(quest.getScientificName());
        descTV.setText(quest.getDescription());
        String dueString;
        if(quest.getDueInt() < 0) {
            dueString = holder.speciesIV.getContext().getString(R.string.quest_expired);
        } else {
            dueString = String.format("%d %s", quest.getDueInt(), holder.speciesIV.getContext().getString(R.string.days_remaining));
        }
//        return dateDifference + " days remaining";
        validTV.setText(dueString);
        Uri uri = Uri.parse(quest.getImage());
        Timber.d(uri.getPath());
        Picasso.with(holder.speciesIV.getContext()).load(uri).resize(100,100).centerInside()
                .placeholder(R.drawable.ic_image_gray_24dp)
                .error(R.drawable.ic_error_black_24dp)
                .into(holder.speciesIV);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), QuestDetailActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("Quest", quest);
                holder.cardView.getContext().startActivity(intent);
            }
        });

//        imageView.setImageURI(uri);

    }

    @Override
    public int getItemCount() {
        return questList.size();
    }

    public void addQuest(Quest quest){
        questList.add(quest);
        notifyItemInserted(questList.size());
    }

    public void updateQuest(Quest quest) {
        if (quest == null) return;
        for (Quest item: questList
             ) {
            if(item.getId() == quest.getId()) {
                int position = questList.indexOf(quest);
                questList.set(position, quest);
                notifyItemChanged(position);
                return;
            }
        }
    }

    public void removeQuest(Quest quest){
        int position = questList.indexOf(quest);
        questList.remove(quest);
        notifyItemRemoved(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView lNameTV, sNameTV, descriptionTV, dueDateTV;
        public ImageView speciesIV;
        public CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_quest);
            lNameTV = itemView.findViewById(R.id.tvLName);
            sNameTV = itemView.findViewById(R.id.tvSName);
            descriptionTV = itemView.findViewById(R.id.quest_description);
            dueDateTV = itemView.findViewById(R.id.quest_submission);
            speciesIV = itemView.findViewById(R.id.ivSpecies);
        }
    }
}
