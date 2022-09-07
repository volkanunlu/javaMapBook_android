package com.volkanunlu.javamap.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Placeholder;
import androidx.recyclerview.widget.RecyclerView;

import com.volkanunlu.javamap.databinding.RecyclerRowBinding;
import com.volkanunlu.javamap.model.Place;
import com.volkanunlu.javamap.view.MainActivity;
import com.volkanunlu.javamap.view.MapsActivity;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {

    List<Place> placeList; //Kullanacağım liste


    public PlaceAdapter(List<Place> placeList){ //constructor metodu
        this.placeList=placeList;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //placeholder döndürdüğüm alan

        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PlaceHolder(recyclerRowBinding);

    }



    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {  //bağlandığı zaman ne olacak

        holder.recyclerRowBinding.recyclerViewTextView.setText(placeList.get(position).name); //ilgili  liste içerisindeki isim gösterilecek


        //tıklanınca intent yapma olayı
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(holder.itemView.getContext(), MapsActivity.class);
                intent.putExtra("info","old"); //var olan bir değeri göstericemi söylüyorum.
                intent.putExtra("place",placeList.get(holder.getAdapterPosition())); //Serialazible yapmadan place sınıfını alamayız bu şekilde custom bir sınıf çünkü.
                holder.itemView.getContext().startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return placeList.size(); //kaç tane mekan varsa o kadar recyclerview oluştur.
    }

    public class PlaceHolder extends RecyclerView.ViewHolder{
        RecyclerRowBinding recyclerRowBinding;



        public PlaceHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding=recyclerRowBinding;

        }
    }

}
