package com.android.tamzeveloper.myweather;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.City;

import java.util.List;

/**
 * Created by tahmina on 15-05-05.
 */
public class NavbarAdapter extends BaseAdapter {


    List<City> locationList;
    Context mContext;




    public NavbarAdapter(Context context, List<City> arrayList){

        locationList=arrayList;
        mContext=context;
    }


    @Override
    public int getCount() {
        return locationList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        NavbarViewHolder navbarViewHolder;

        if(convertView == null){

            if(position==locationList.size()-1){
                view= LayoutInflater.from(mContext).inflate(R.layout.list_item_navbar_add_settings,parent,false);
            }else
                view= LayoutInflater.from(mContext).inflate(R.layout.list_item_navbar,parent,false);


            navbarViewHolder=new NavbarViewHolder(view);
            view.setTag(navbarViewHolder);

        }else{

            view=convertView;
            navbarViewHolder=(NavbarViewHolder)view.getTag();
        }


        navbarViewHolder.mTextView.setText(locationList.get(position).getName());
        navbarViewHolder.mTextCountryView.setText(locationList.get(position).getCountry());

        if(position==0)
            navbarViewHolder.mIcon.setImageResource(R.drawable.ic_communication_location_on);
        else if(position==locationList.size()-1)
            navbarViewHolder.mIcon.setImageResource(R.drawable.ic_content_add);
        else

        navbarViewHolder.mIcon.setImageResource(R.drawable.ic_action_delete);


        Log.d("TAMZ","In NavbarAdapter : "+position +" City : "+locationList.get(position).getName());


        return view;
    }


    private class NavbarViewHolder{
        TextView mTextView;
        TextView mTextCountryView;
        ImageView mIcon;

         NavbarViewHolder(View view){
            mTextView=(TextView)view.findViewById(R.id.navListText);
            mTextCountryView=(TextView)view.findViewById(R.id.navListSubText);
            mIcon=(ImageView)view.findViewById(R.id.navListTextIcon);
        }
    }

}
