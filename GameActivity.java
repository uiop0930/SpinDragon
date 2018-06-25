package com.mingky.spindragon;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class GameActivity extends AppCompatActivity {


    GameView gv;

    TextView tvScore;
    TextView tvCoin;
    TextView tvGem;
    TextView tvBomb;
    TextView tvChampion;

    View dialog= null;

    Animation ani;

    MediaPlayer mp;

    ToggleButton tbMusic, tbSound, tbVibrate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gv= findViewById(R.id.gv);

        tbMusic= findViewById(R.id.tb_music);
        tbSound= findViewById(R.id.tb_sound);
        tbVibrate= findViewById(R.id.tb_vibrate);

        tvScore= findViewById(R.id.tv_score);
        tvCoin= findViewById(R.id.tv_coin);
        tvGem= findViewById(R.id.tv_gem);
        tvBomb= findViewById(R.id.tv_bomb);
        tvChampion= findViewById(R.id.champion);

        mp= MediaPlayer.create(this, R.raw.my_friend_dragon);
        mp.setLooping(true);    //반복

        tbMusic.setOnCheckedChangeListener(checkedChangeListener);
        tbSound.setOnCheckedChangeListener(checkedChangeListener);
        tbVibrate.setOnCheckedChangeListener(checkedChangeListener);

        tbMusic.setChecked(G.isMusic);  //시작할때는 true로 시작하도록!
        tbSound.setChecked(G.isSound);
        tbVibrate.setChecked(G.isVibrate);

    }

    CompoundButton.OnCheckedChangeListener checkedChangeListener= new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

            switch (compoundButton.getId()){
                case R.id.tb_music:
                    G.isMusic= checked;
                    if(G.isMusic) mp.setVolume(0.5f,0.5f);
                    else mp.setVolume(0,0);
                    break;

                case R.id.tb_sound:
                    G.isSound= checked;
                    break;

                case R.id.tb_vibrate:
                    G.isVibrate= checked;
                    break;

            }
        }
    };


    @Override
    protected void onResume() {
        if(mp != null){
            if(G.isMusic) mp.setVolume(0.5f, 0.5f);
            else mp.setVolume(0,0);
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(mp != null){
            mp.stop();
            mp.release();
            mp= null;
        }
        super.onDestroy();
    }

    //Activity가 화면에서 보이지 않게 되면 자동으로 실행되는 메소드
    @Override
    protected void onPause() {

        if(mp != null) mp.pause();

        gv.pauseGame();
        super.onPause();
    }

//    @Override                 --→ 원래는 onResume()이 맞지만, 이 앱에서는 사용하지말자!
//    protected void onResume() {
//        super.onResume();
//    }


    //Activity의 뒤로가기버튼(BackButton)을 클릭했을 때 자동으로 실행되는 메소드
    @Override
    public void onBackPressed() {
//        super.onBackPressed();      //super! 를 하면 실행 왕ㄴ료!

        //하지만 다이얼로그를 띄워야한다.
        if(dialog!= null) return;

        gv.pauseGame();

        //Dialog 보이기
        dialog= findViewById(R.id.dialog_quit);
        dialog.setVisibility(View.VISIBLE);

        ani= AnimationUtils.loadAnimation(this, R.anim.appear_dialog_quit );
        dialog.startAnimation(ani);


    }


    public void clickPause(View v){

        if(dialog!= null) return;

        gv.pauseGame();

        dialog= findViewById(R.id.dialog_pause);
        dialog.setVisibility(View.VISIBLE);

        ani= AnimationUtils.loadAnimation(this, R.anim.appear_dialog_pause);
        dialog.startAnimation(ani);

    }

    public void clickQuit(View v){

        if(dialog!= null) return;

        gv.pauseGame();

        //Dialog 보이기
        dialog= findViewById(R.id.dialog_quit);
        dialog.setVisibility(View.VISIBLE);

        ani= AnimationUtils.loadAnimation(this, R.anim.appear_dialog_quit );
        dialog.startAnimation(ani);

    }

    public void clickShopClass(View v){

        appearDialog(R.id.dialog_shop);

    }

    public void clickShopItem(View v){

        appearDialog(R.id.dialog_shop);

    }

    public void clickSetting(View v){

        appearDialog(R.id.dialog_setting);

    }


    //dialog 보이는 작업 메소드
    void appearDialog(int resId){
        if(dialog!= null) return;
        gv.pauseGame();

        dialog= findViewById(resId);
        dialog.setVisibility(View.VISIBLE);

        ani= AnimationUtils.loadAnimation(this, R.anim.appear_dialog);
        dialog.startAnimation(ani);

    }

    void disappearDialog(){

        ani= AnimationUtils.loadAnimation(this, R.anim.disappear_dialog);
        ani.setAnimationListener(animationListener);

        dialog.startAnimation(ani);

    }



    public void clickBtn(View v){

        switch(v.getId()){

            case R.id.dialog_setting_check:
                disappearDialog();
                break;


            case R.id.dialog_shop_check:
                disappearDialog();
                break;

            case R.id.dialog_pause_play:
//                dialog.setVisibility(View.GONE);      GONE은 지금하면 애니메이션이 보이지 않으므로 애니메이션이 끝나면 실행되도록 한다.
//                dialog= null;

                ani= AnimationUtils.loadAnimation(this, R.anim.disappear_dialog_pause);
                ani.setAnimationListener(animationListener);

                dialog.startAnimation(ani);

                break;


            case R.id.dialog_quit_ok:
                //게임종료
//                finish();   //이상태만으로는 gameThread가 아직 돌아가는중이므로 오류! 다 종료가 되어야한다!

                //그래서 반드시 그냥 끄지 말고 GameThread의 동작을 종료시켜야한다.
//                gv.gameThread.isRun= false;   이런식으로 직접 건드리지 마세요!
                gv.stopGame();  //새로 메소드를 만들어서 그곳에서 제어하자.
                finish();       //그 다음 finish();

                break;


            case R.id.dialog_quit_cancel:
                dialog.setVisibility(View.GONE);
                dialog= null;   //중요! null을 하지 않으면 다른것을 참조하고있기 때문!

                gv.resumeGame();

                break;


        }//switch

    }//clickBtn method


    Animation.AnimationListener animationListener= new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

            dialog.setVisibility(View.GONE);
            dialog= null;

            gv.resumeGame();

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };


    //GameThread로부터 메세지를 전달받는 객체 생성
    Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {

            gv.stopGame();  //Game Over 이므로 wait이아닌 stop!

            Bundle data= msg.getData();

            Intent intent= new Intent(GameActivity.this, GameoverActivity.class);
            intent.putExtra("Data", data);
            startActivity(intent);

            finish();

        }
    };



}//GameActivity class
