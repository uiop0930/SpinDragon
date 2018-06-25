package com.mingky.spindragon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameoverActivity extends AppCompatActivity {


    ImageView iv;

    TextView tvChampion;
    TextView tvYourScore;

    boolean isChampion= false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);

        iv= findViewById(R.id.iv);
        tvChampion= findViewById(R.id.tv_champion);
        tvYourScore= findViewById(R.id.tv_yourscore);


        Intent intent= getIntent();
        Bundle data= intent.getBundleExtra("Data");

        int score= data.getInt("Score", 0);
        int coin=  data.getInt("Coin" , 0);

        int yourScore= score + coin*10;

        String s= String.format("%07d", yourScore);
        tvYourScore.setText(s);


        if(yourScore > G.champion ){
            G.champion= yourScore;
            isChampion= true;
        }

        s= String.format("%07d", G.champion);
        tvChampion.setText(s);


        //챔피언이미지가 있는가?
        if(G.championImg!=null){
            Uri uri= Uri.parse(G.championImg);
            iv.setImageURI(uri);

        }


    }


    public void clickRetry(View v){
        Intent intent= new Intent(this, GameActivity.class);
        startActivity(intent);

        finish();
    }

    public void clickExit(View v){
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();

    }

    void saveData(){
        SharedPreferences pref= getSharedPreferences("Data", MODE_PRIVATE/*다른모드에서 이 문서를 건드릴 수 없다는 의미*/);
        SharedPreferences.Editor editor= pref.edit();   //xml을 작성해주는 친구

        editor.putInt("Gem", G.gem);
        editor.putInt("Champion", G.champion);

        editor.putBoolean("Music", G.isMusic);
        editor.putBoolean("Sound", G.isSound);
        editor.putBoolean("Vibrate", G.isVibrate);

        editor.putString("ChampionImg", G.championImg);

        editor.commit();    //많이 쓰는 문장. "완료했다!" 라는 의미
    }

    public void clickImg(View v){
        if( !isChampion ) return;

        //디바이스의 사진을 선택하도록...
        //Gallery앱 or 사진앱을 실행!
        Intent intent= new Intent();            //이름을 모르겠으면 이름을 적지 마세요
        intent.setAction(Intent.ACTION_PICK);   //고르게 할 수 있다!
        intent.setType("image/*");              //"video"라고 쓰면 비디오, "audio"를 쓰면 오디오 중에 고르도록 설정시킨다.

        startActivityForResult(intent, 10);   //선택 후 결과를 가져오려면 이 기능이 반드시 필요!

    }


    //startActivityForResult()로 실행한 Activity가 종료되면 자동으로 실행되는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);      //지워도 되고 놔둬도 됩니당!

        switch (requestCode){
            case 10:    //requestCode(식별코드) --→ 내가 부른 것이 맞습니까?
                if(resultCode==RESULT_OK){  //선택 안하고 뒤로가기누르면 cancel된다!

//                    Uri uri= data.getData();
//                    iv.setImageURI(uri);      //이게 원래 전통! 근데 기종마다 다르기 때문에 안될 수 있다. 주석!

                    Uri uri= data.getData();
                    if(uri != null){
                        Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();    //uri.toString()을 하면 경로가 쭉 다 나온다.
                        G.championImg= uri.toString();
                        iv.setImageURI(uri);      //오류나기 좋은코드..!
                        // 이 친구는 주소로 오는것
                    }else{
                        Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();       //else면 null
                        Bundle bundle= data.getExtras();
                        Bitmap bm= (Bitmap)bundle.get("data");     //"data"는 구글에서 정해진 이름

                        iv.setImageBitmap(bm);
                        //이 친구는 Bitmap으로 오는것
                    }

                }

                break;


        }

    }
}


