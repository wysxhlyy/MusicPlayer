package com.example.victor.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private playService.MyBinder myMusicService;
    private File chosenMusic;


    private Button stop;
    private Button pp;
    private Button quit;
    private Button minimize;
    private Button playMethod;

    private SeekBar progress;
    private int musicProgress;
    private ListView lv;
    private String[] strs;
    private Intent intent;
    private File list[];
    private TextView musicTime;
    private TextView musicProgTime;
    private Handler h= new Handler();
    private int musicPos;
    private TextView playingMusic;
    private boolean sequencial;


    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("g54mdp", "MainActivity onServiceConnected");
            checkProgress();
            myMusicService=(playService.MyBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("g54mdp", "MainActivity onServiceDisconnected");
            myMusicService=null;
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialComponent();
        intent=new Intent(MainActivity.this,playService.class);
        startService(intent);
        this.bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d("g53mdp","bind service");

        chooseMusic();

        stop.setOnClickListener(this);
        pp.setOnClickListener(this);
        minimize.setOnClickListener(this);
        quit.setOnClickListener(this);

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    myMusicService.setMusicProgress(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                myMusicService.pauseMusic();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myMusicService.playMusic();
            }
        });

        sequencial=true;

    }



    public void onClick(View view){
        switch (view.getId()){
            case R.id.stop:
                myMusicService.stopMusic();
                break;
            case R.id.pp:
                if(myMusicService.musicState()== MP3Player.MP3PlayerState.PLAYING){
                    myMusicService.pauseMusic();
                    pp.setText("play");
                }else if(myMusicService.musicState()== MP3Player.MP3PlayerState.PAUSED){
                    myMusicService.playMusic();
                    pp.setText("pause");
                }
                checkProgress();
                break;
            case R.id.quit:
                quitWarn();
                break;
            case R.id.minimize:
                if(serviceConnection!=null){
                    unbindService(serviceConnection);
                    serviceConnection=null;
                }
                finish();
                break;
            case R.id.playMethod:
                if(sequencial){
                    sequencial=false;
                    playMethod.setText("random");
                }else {
                    sequencial=true;
                    playMethod.setText("sequencial");
                }
                break;
        }
    }

    public void quitWarn(){
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Quit");
        builder.setMessage("Are you sure to stop the music?");

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this,"Continue to enjoy your music",Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stopService(intent);
                myMusicService.stopMusic();
                finish();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    public void initialComponent(){
        pp=(Button)findViewById(R.id.pp);
        stop=(Button)findViewById(R.id.stop);
        quit=(Button)findViewById(R.id.quit);
        minimize=(Button)findViewById(R.id.minimize);
        musicTime=(TextView)findViewById(R.id.musicTime);
        musicProgTime=(TextView)findViewById(R.id.musicProgTime);
        progress=(SeekBar)findViewById(R.id.progress);
        lv = (ListView) findViewById(R.id.listView);
        playingMusic=(TextView)findViewById(R.id.playingMusic);
        playMethod=(Button) findViewById(R.id.playMethod);
    }


    public void chooseMusic(){
        File musicDir = new File( Environment.getExternalStorageDirectory().getPath()+ "/Music/");
        list = musicDir.listFiles();
        strs=new String[list.length];
        for(int i=0;i<list.length;i++){
            strs[i]=list[i].getName();
        }
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strs));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                musicPos=myItemInt;
                String selectedFromName=(String)(lv.getItemAtPosition(myItemInt));
                File selectedFromList=null;
                for(int i=0;i<list.length;i++){
                    if(list[i].getName().equals(selectedFromName)){
                        selectedFromList=list[i];
                    }
                }
                chosenMusic=selectedFromList;
                if(myMusicService==null){
                    try{
                        myMusicService.loadMusic(selectedFromList.getAbsolutePath());
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this,"Fail to load music,Click Again",Toast.LENGTH_SHORT).show();
                    }
                    myMusicService.playMusic();
                }else{
                    myMusicService.stopMusic();
                    try{
                        myMusicService.loadMusic(selectedFromList.getAbsolutePath());
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this,"Fail to load music,Click Again",Toast.LENGTH_SHORT).show();
                    }
                    myMusicService.playMusic();
                    progress.setProgress(myMusicService.musicProgress());
                }

                musicTime.setText(msToMin(myMusicService.musicDuration()));
                checkProgress();
            } });
    }


    public String msToMin(long ms){
        SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
        return sdf.format(ms);
    }


    public void checkProgress(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                progress.setMax(myMusicService.musicDuration());
                while(myMusicService.musicProgress()<=myMusicService.musicDuration()){
                    musicProgress=myMusicService.musicProgress();
                    progress.setProgress(musicProgress);
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            musicProgTime.setText(msToMin(myMusicService.musicProgress()));
                            if(myMusicService.musicState()!= MP3Player.MP3PlayerState.STOPPED){
                                playingMusic.setText(list[musicPos].getName());
                                musicTime.setText(msToMin(myMusicService.musicDuration()));
                            }
                        }
                    });

                    try {
                        Thread.sleep(1000);
                        if(sequencial){
                            sequencialPlay();
                        }else {
                            randomPlay();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void sequencialPlay(){
        if(myMusicService.musicProgress()>=(myMusicService.musicDuration()-1500)&& myMusicService.musicState()== MP3Player.MP3PlayerState.PLAYING){
            Log.d("g53mdp","come in") ;
            String selectedFromName=" ";
                if(musicPos==list.length-1){
                    musicPos=-1;
                }
                selectedFromName=(String)(lv.getItemAtPosition(musicPos+1));
                File nextMusic=null;
                for(int i=0;i<list.length;i++){
                    if(list[i].getName().equals(selectedFromName)){
                        nextMusic=list[i];
                    }
                }
                musicPos++;
                myMusicService.stopMusic();
                try{
                    myMusicService.loadMusic(nextMusic.getAbsolutePath());
                }catch (Exception e){
                    Toast.makeText(MainActivity.this,"Fail to load music,try again",Toast.LENGTH_SHORT).show();
                }
                myMusicService.playMusic();
                progress.setMax(myMusicService.musicDuration());

        }
    }

    public void randomPlay(){
        if(myMusicService.musicProgress()>=(myMusicService.musicDuration()-1000)&& myMusicService.musicState()== MP3Player.MP3PlayerState.PLAYING){
            Log.d("g53mdp","come in") ;
            String selectedFromName=" ";
            Random randomGenerator=new Random();
            musicPos=randomGenerator.nextInt(list.length);
            Log.d("test",musicPos+"");

            selectedFromName=(String)(lv.getItemAtPosition(musicPos));
            File nextMusic=null;
            for(int i=0;i<list.length;i++){
                if(list[i].getName().equals(selectedFromName)){
                    nextMusic=list[i];
                }
            }
            myMusicService.stopMusic();
            try{
                myMusicService.loadMusic(nextMusic.getAbsolutePath());
            }catch (Exception e){
                Toast.makeText(MainActivity.this,"Fail to load music,try again",Toast.LENGTH_SHORT).show();
            }
            myMusicService.playMusic();
        }
    }




    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onDestroy(){
        Log.d("g54mdp","Activity Destroyed");
        if(serviceConnection!=null){
            unbindService(serviceConnection);
            serviceConnection=null;
        }
        super.onDestroy();

    }
}
