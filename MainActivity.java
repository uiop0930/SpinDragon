package com.mingky.spindragon;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp= MediaPlayer.create(this, R.raw.dragon_flight);  //이 한 줄로 음악이 들어갔다! --→ (new Thread()와 똑같음)
        mp.setLooping(true);    //계속 반복할거야? --→ 빠지지않는 문장!

//        mp.setVolume(0.5f, 0.5f);         //이 문장으로 하드웨어의 음원이 아닌 게임의 사운드 음원을 조절할 수 있다.
//        //처음 시작할 때 70%(0.7f)정도로 많이 만들어놓는다! (default값)

//        mp.start();
//        //화면이 꺼지면 노래도 꺼지도록 해야한다. 이 상태로 두면 앱을 삭제해야 노래가 꺼진다.
//        //Resume()에서 start()를 하기 때문에 여기서 할 필요가 없다.
    }


    @Override
    protected void onResume() {
        if(G.isMusic==true){
            mp.setVolume(0.5f, 0.5f);
        }else {
            mp.setVolume(0, 0);
        }
        mp.start();     //resume()이 없기 때문에 그냥 start()하면 된다.

        super.onResume();
    }

    @Override
    protected void onPause() {
        if( mp != null && mp.isPlaying() ){     //둘 다 같은말
//            mp.stop();    //stop()이 아니라 잠시 멈추도록 하자
            mp.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(mp != null ){
            mp.stop();
            mp.release();
            mp= null;   //이 친구는 Thread이기 때문에 이 문장 한 줄 만으로는 완전히 멈추지 않는다! stop해주자
        }

        super.onDestroy();
    }

    public void clickStart(View v){
        Intent intent= new Intent(this, GameActivity.class);
        startActivity(intent);

    }

    public void clickExit(View v){
        finish();

    }

}
