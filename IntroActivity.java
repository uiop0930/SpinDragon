package com.mingky.spindragon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity {


    ImageView iv;

    //스케쥴관리 객체(비서 객체)
    Timer timer= new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        iv= findViewById(R.id.iv);

        //View에게 Animation 효과를 주는 객체 생성
        //appear_logo.xml문서를 읽어서 Animation객체로 생성
        Animation ani= AnimationUtils.loadAnimation(this, R.anim.appear_logo);
        //바로 아래의 주석을 쓰는 것 보다 이 기법으로 사용하는것이 더 수월!

//        AlphaAnimation ani= new AlphaAnimation(0.0f, 1.0f);
//        //Animation ani= new Animation(0.0f, 1.0f);     도 가능!
//        ani.setDuration(3000);  //3초동안 실행

        iv.startAnimation(ani);


        //4초후에 MainActivity 실행!


        //스케쥴관리 객체에게 스케쥴 등록
        timer.schedule(task, 4000);

        //생성자가 끝나기전에 저장된 데이터들 로딩하기
        loadData();

    }//생성자

    void loadData(){
        SharedPreferences pref= getSharedPreferences("Data", MODE_PRIVATE);

        G.gem= pref.getInt("Gem", 0);
        G.champion= pref.getInt("Champion", 0);

        G.isMusic= pref.getBoolean("Music", true);
        G.isSound= pref.getBoolean("Sound", true);
        G.isVibrate= pref.getBoolean("Vibrate", true);

        G.championImg= pref.getString("ChampionImg", null);

    }

    //timer의 스케쥴링 작업을 수행하는 객체 생성
    TimerTask task= new TimerTask() {
        @Override
        public void run() {     //run()! 이 친구는 Thread다!
            //스케쥴링에 의해 4초 후에 이 메소드 실행
            Intent intent= new Intent(IntroActivity.this, MainActivity.class );
            startActivity(intent);
            finish();
        }
    };

}
