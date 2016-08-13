package musicgenie.com.musicgenie.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import musicgenie.com.musicgenie.R;
import musicgenie.com.musicgenie.activity.DowloadsActivity;
import musicgenie.com.musicgenie.adapters.LiveDownloadListAdapter;
import musicgenie.com.musicgenie.handlers.TaskHandler;
import musicgenie.com.musicgenie.models.DownloadTaskModel;
import musicgenie.com.musicgenie.utilities.App_Config;
import musicgenie.com.musicgenie.utilities.SharedPrefrenceUtils;


public class ActiveTaskFragment extends Fragment {


    private static final String TAG = "ActiveTaskFragment";
    private ListView liveDownloadListView;
    private LiveDownloadListAdapter adapter;
    private ProgressUpdateBroadcastReceiver receiver;

    public ActiveTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView =inflater.inflate(R.layout.fragment_active_task, container, false);
        liveDownloadListView = (ListView) fragmentView.findViewById(R.id.liveDownloadListView);
        adapter = new LiveDownloadListAdapter(getActivity());
        adapter.setDownloadingList(getTasksList());
        liveDownloadListView.setAdapter(adapter);

        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SharedPrefrenceUtils.getInstance(activity).setActiveFragmentAttachedState(true);
        registerForBroadcastListen(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPrefrenceUtils.getInstance(getActivity()).setActiveFragmentAttachedState(false);
        unRegisterBroadcast();
    }

    private ArrayList<DownloadTaskModel> getTasksList(){
        ArrayList<DownloadTaskModel> list = new ArrayList<>();
        //get tasks list from taskhandler
        //get title from sf
        ArrayList<String> taskIDs = TaskHandler.getInstance(getActivity()).getTaskSequence();
        for(String t_id : taskIDs) {
            String title = SharedPrefrenceUtils.getInstance(getActivity()).getTaskTitle(t_id);
            list.add(new DownloadTaskModel(title,0,t_id));
        }
        return list;
    }


    private int getPosition(String taskID){
        int pos=-1;
        ArrayList<DownloadTaskModel> list = getTasksList();
        for(int i=0;i<list.size();i++){
            if(list.get(i).taskID.equals(taskID)){
                pos=i;
                return pos;
            }
        }
        return pos;
    }

    private void updateItem(int position,int progress){

        if(position!=-1){
        ArrayList<DownloadTaskModel> old_list = getTasksList();
        for(int i=0;i<old_list.size();i++){
            if(i==position){
                old_list.set(i,new DownloadTaskModel(old_list.get(i).Title,progress,old_list.get(i).taskID));
            }
        }

        adapter.setDownloadingList(old_list);
        liveDownloadListView.setAdapter(adapter);

        int start = liveDownloadListView.getFirstVisiblePosition();
        int end = liveDownloadListView.getLastVisiblePosition();

            if(start<=position && end>=position){
                log("updating "+position+"with "+progress+" %");

                View view = liveDownloadListView.getChildAt(position);
                liveDownloadListView.getAdapter().getView(position,view,liveDownloadListView);
            }
        }
        else{
            // refressing the tasks list
            adapter.setDownloadingList(getTasksList());
            liveDownloadListView.setAdapter(adapter);

        }
    }


    public class ProgressUpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            log("update via br " + intent.getStringExtra(App_Config.EXTRA_PROGRESS));

            String taskID = intent.getStringExtra(App_Config.EXTRA_TASK_ID);
            String progress = intent.getStringExtra(App_Config.EXTRA_PROGRESS);

            updateItem(getPosition(taskID),Integer.valueOf(progress));
        }
    }

    private void registerForBroadcastListen(Activity activity) {
        receiver = new ProgressUpdateBroadcastReceiver();
        activity.registerReceiver(receiver, new IntentFilter(App_Config.ACTION_PROGRESS_UPDATE_BROADCAST));
    }

    private void unRegisterBroadcast() {
        getActivity().unregisterReceiver(receiver);
    }

    public void makeToast(String msg){

        Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();

    }
    public void log(String msg){
        Log.d(TAG,msg);
    }
}
