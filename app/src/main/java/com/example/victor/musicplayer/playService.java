package com.example.victor.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class playService extends Service {


    public playService() {
    }

    private  IBinder binder;
    private  MP3Player player;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public void onCreate(){
        super.onCreate();
        if(player!=null){
            player=null;
        }
        binder=new MyBinder();
        player=new MP3Player();

        //create the Notification
        PendingIntent pi=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);
        Resources r=getResources();
        Notification notification=new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.music)
                .setContentTitle("MP3Player")
                .setContentText("Your MP3Player is working")
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_SOUND)
                .build();

        NotificationManager nManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nManager.notify(0,notification);
    }

    public class MyBinder extends Binder{

        void playMusic(){
            playService.this.playMusic();
        }

        void loadMusic(String path){
                playService.this.loadMusic(path);
        }

        void stopMusic(){
            playService.this.stopMusic();
        }

        void pauseMusic(){
            playService.this.pauseMusic();
        }

        int musicProgress(){
            return playService.this.musicProgress();
        }

        int musicDuration(){
            return playService.this.musicDuration();
        }

        MP3Player.MP3PlayerState musicState(){
            return playService.this.musicState();
        }

        void setMusicProgress(int progress){
            playService.this.setMusicProgress(progress);
        }
    }

    public int onStartCommand(Intent intent,int flags,int startId){


        return super.onStartCommand(intent,flags,startId);
    }


    public void playMusic(){
            player.play();
    }

    public void loadMusic(String path){
        player.stop();
        player.load(path);
    }

    public void stopMusic(){
        player.stop();
    }

    public void pauseMusic(){
        player.pause();
    }

    public MP3Player.MP3PlayerState musicState(){
        return player.getState();
    }

    public int musicProgress(){

        return player.getProgress();
    }

    public int musicDuration(){
        return player.getDuration();
    }

    public void setMusicProgress(int progress){
        player.setMusicProgress(progress);
    }


}
