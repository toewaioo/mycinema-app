package com.two.channelmyanmar.adapter;

/*
 * Created by Toewaioo on 4/27/25
 * Description: [Add class description here]
 */

import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.content.Context;
import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RadioButton;

import android.widget.TextView;

import com.two.channelmyanmar.R;
import com.two.channelmyanmar.api.FileSizeTask;
import com.two.channelmyanmar.model.MeganzModel;

public class MeganzAdapter extends ArrayAdapter<MeganzModel>{

    int selectedIndex=-1;
    public MeganzAdapter(Context c,int res,ArrayList<MeganzModel> file){
        super(c,res,file);
    }
    public int getSelectIndex(){
        return selectedIndex;
    }
    public MeganzModel getdata(int position){
        MeganzModel result= getItem(position);
        return result;
    }
    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v=convertView;
        if(v==null){
            LayoutInflater vi=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.watch_sheet_adapter_layout, null,true);

        }
        RadioButton rbSelect=v.findViewById(R.id.watchSheetSiteRadio);
        if(selectedIndex == position){  // check the position to update correct radio button.
            rbSelect.setChecked(true);
        }
        else{
            rbSelect.setChecked(false);
        }
        MeganzModel data= (getItem(position));
        if(data!=null){
            TextView format=v.findViewById(R.id.watchSheetQuality);
            final TextView size=v.findViewById(R.id.watchSheetsize);

            //  final ProgressBar pb=v.findViewById(R.id.youtubeadapterProgressBar1);
            format.setText(data.getType());
            //size.setText(data.getSize());
            rbSelect.setText(data.getQuality());
            if(data.getSize().isEmpty()){

                Thread t=new Thread(new FileSizeTask(data.getUrl(), new FileSizeTask.CallBack() {
                    @Override
                    public void onSuccess(String length) {
                        try{
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    size.setVisibility(View.VISIBLE);
                                    // pb.setVisibility(View.INVISIBLE);
                                    size.setText(length);
                                }
                            });
                        }catch (Exception e){

                        }


                    }

                    @Override
                    public void onFail(String msg) {

                    }
                }));
                t.start();

            }else{
                size.setVisibility(View.VISIBLE);
                // pb.setVisibility(View.INVISIBLE);
                size.setText(data.getSize());


            }

        }



        return v;
        //return super.getView(position, convertView, parent);
    }





}

