package com.mingky.spindragon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiFunction;

/**
 * Created by alfo06-10 on 2018-03-28.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback{


    Context context;
    SurfaceHolder holder;

    int width, height;
    GameThread gameThread;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context= context;

        holder= getHolder();
        holder.addCallback(this);   //GameView는 이미 CallBack기능을 갖고있다! --→ this
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //생성자가 끝나고 이 GameView가 화면에 보여지면 자동 호출

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //surfaceCreated()가 실행된 후 자동 실행
        //보통 이 때 Game진행 작업을 시작한다!
        if (gameThread==null){      //객체가 비어있니? ---→ 처음 시작했니?
            width = getWidth();
            height= getHeight();

            gameThread= new GameThread();
            gameThread.start();

        }else{
            //게임 재시작(resume)
            gameThread.resumeThread();

        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //GameView가 화면에서 보이지 않으면 자동 실행

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {


        int action= event.getAction();
        int x, y;   //원래 좌표는 float으로 잡는게 더 정확합니다! int면 오차가 좀 있습니다.


        switch (action){

            case MotionEvent.ACTION_DOWN:
                x= (int)event.getX();
                y= (int)event.getY();

                gameThread.touchDown(x, y);

                break;

            case MotionEvent.ACTION_UP:
                x= (int)event.getX();
                y= (int)event.getY();

                gameThread.touchUp(x, y);

                break;

            case MotionEvent.ACTION_MOVE:
                x= (int)event.getX();
                y= (int)event.getY();

                gameThread.touchMove(x, y);

                break;

        }

        return true;

    }



    void stopGame(){
//        gameThread.isRun= false;  --→ 건드리지 말자! 각자의 것만 제어하자.
        gameThread.stopThread();

    }

    void pauseGame(){
        gameThread.pauseThread();

    }

    void resumeGame(){
        gameThread.resumeThread();

    }



    //Inner class
    //실제 Game의 모든 작업을 수행하는 직원객체(Thread)
    class GameThread extends Thread{

        boolean isRun= true;
        boolean isWait= false;


        Bitmap imgBack;
        Bitmap imgJoypad;
        Bitmap[] imgMissile= new Bitmap[3];
        Bitmap[][] imgPlayer= new Bitmap[3][4];
        Bitmap[][] imgEnemy= new Bitmap[3][4];

        Bitmap[][] imgGauge= new Bitmap[2][];
        //한 층에 yellow는 5칸, pink는 3칸! (c언어때에는 안되지만, java는 됩니다!)

        Bitmap[] imgDust= new Bitmap[6];    //먼지가 제각각 사이즈를 가지고 적군이 사라질때마다 나타나도록!
        Bitmap[] imgItem= new Bitmap[7];

        Bitmap imgProtect;
        Bitmap imgStrong;
        Bitmap imgBomb;


        //폭탄버튼 변수들
        Rect rectBomb;
        boolean isBomb= false;     //폭탄버튼을 눌렀는가?

        int protectRad;     //보호막이미지의 반지름
        int protectAng;     //보호막이미지의 회전각도


        int positionBack=0;     //배경이미지의 x좌표

        //조이패드 변수들
        int jpx, jpy;   //조이패드이미지의 중심좌표
        int jpr;        //조이패드이미지의 반지름
        boolean isJoypad= false;   //조이패드를 눌렀는가?

        //Bitmap의 투명도(alpha)를 적용하기 위한 Paint
        Paint paint= new Paint();

        //플레이어 객체 참조변수
        Player me;
        int playerKind= 0;      //플레이어 종류


        //미사일들 리스트 객체
        ArrayList<Missile> missiles= new ArrayList<>();

        //적군들 리스트 객체
        ArrayList<Enemy> enemies= new ArrayList<>();

        //먼지들 리스트 객체
        ArrayList<Dust> dusts= new ArrayList<>();

        //아이템들 리스트 객체
        ArrayList<Item> items= new ArrayList<>();


        Random rnd= new Random();

        //미사일 발사 간격
        int missileGap= 3;
        int level= 1;

        //아이템의 지속시간
        int    fastTime= 0;
        int protectTime= 0;
        int  magnetTime= 0;
        int  strongTime= 0;

        int  bomb= 3;    //시작 폭탄 갯수
        int score= 0;    //시작 점수
        int  coin= 0;    //코인당 점수 10점


        SoundPool sp;
        int sdChDie, sdFireBall, sdCoin, sdGem, sdProtect, sdItem, sdMonDie;   //캐릭터 죽음

        Vibrator vibrator;  //진동 관리자



        //초기값 설정 작업 메소드(마치 생성자처럼)
        void init(){
            //그림들을 Bitmap객체 생성
            createBitmaps();

            //플레이어 객체 생성
            me= new Player(width, height, imgPlayer, playerKind);

            //조이패드 초기값
            jpx= width- jpr;
            jpy= height- jpr;

            //TextView들의 값 설정!
            setTextView();


            //효과음 변수들 작업
            sp= new SoundPool(10, AudioManager.STREAM_MUSIC, 0/*기본값*/);

            sdChDie= sp.load(context, R.raw.ch_die, 1);
            sdFireBall= sp.load(context, R.raw.fireball, 1);
            sdCoin= sp.load(context, R.raw.get_coin, 1);
            sdGem= sp.load(context, R.raw.get_gem, 1);
            sdProtect= sp.load(context, R.raw.get_invincible, 1);
            sdItem= sp.load(context, R.raw.get_item, 1);
            sdMonDie= sp.load(context, R.raw.mon_die, 1);

            //진동 관리자 객체 얻어오기
//            vibrator= context.getVibrator.....  //직접 받아오는 기능이 없음
            vibrator= (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        }


        //GameActivity의 TextView에 값 설정
        void setTextView(){

            //1. Handler    2.runOnUIThread() ←-- Activity메소드
            //3. ActivityClass의 runOnUIThread()와 같은 역할을 하는 메소드
            post(new Runnable() {   //전달하다. 전송하다.
                @Override
                public void run() {

                    GameActivity ga= (GameActivity) context;

                    String s;

                    s= String.format("%07d", score);
                    ga.tvScore.setText(s);

                    s= String.format("%04d", coin);
                    ga.tvCoin.setText(s);

                    s= String.format("%04d", G.gem);
                    ga.tvGem.setText(s);

                    s= String.format("%04d", bomb);
                    ga.tvBomb.setText(s);

                    s= String.format("%07d", G.champion);
                    ga.tvChampion.setText(s);

                }
            });

        }


        //그림을 Bitmap객체로 만들어내는 작업 메소드
        void createBitmaps(){

            Resources res= context.getResources();

            //폭탄버튼이미지 만들기
            int sizeBomb= height/5;

            imgBomb= BitmapFactory.decodeResource(res, R.drawable.btn_bomb);
            imgBomb= Bitmap.createScaledBitmap(imgBomb, sizeBomb, sizeBomb, true);
            rectBomb= new Rect(0, height- sizeBomb, sizeBomb, height);


            //보호막이미지 만들기
            imgProtect= BitmapFactory.decodeResource(res, R.drawable.effect_protect);
            imgProtect= Bitmap.createScaledBitmap(imgProtect, height/4, height/4, true);

            protectRad= imgProtect.getWidth()/2;


            //강화 미사일이미지 만들기
            imgStrong= BitmapFactory.decodeResource(res, R.drawable.bullet_04);
            imgStrong= Bitmap.createScaledBitmap(imgStrong, height/10, height/10, true);


            //아이템이미지 만들기
            for(int i=0; i<7; i++){
                imgItem[i]= BitmapFactory.decodeResource(res, R.drawable.item_0_coin+i);
                imgItem[i]= Bitmap.createScaledBitmap(imgItem[i], height/16, height/16, true);
            }


            //먼지이미지 만들기
            Bitmap img= BitmapFactory.decodeResource(res, R.drawable.dust);
            float[] ratio= new float[]{0.8f, 1.0f, 1.5f, 0.1f, 0.3f, 1.3f};     //먼지들의 크기

            for(int i=0; i<6; i++){
                int size= (int)(height/9 * ratio[i]);   //적군사이즈와 같도록
                imgDust[i]= Bitmap.createScaledBitmap(img, size, size,true);
            }


            //게이지이미지 만들기
            imgGauge[0]= new Bitmap[5];
            for(int i=0; i<5; i++){
                imgGauge[0][i]= BitmapFactory.decodeResource(res, R.drawable.gauge_step5_01+i);
                imgGauge[0][i]= Bitmap.createScaledBitmap(imgGauge[0][i], height/9, height/36, true);
            }

            imgGauge[1]= new Bitmap[3];

            for(int i=0; i<3; i++){
                imgGauge[1][i]= BitmapFactory.decodeResource(res, R.drawable.gauge_step3_01+i);
                imgGauge[1][i]= Bitmap.createScaledBitmap(imgGauge[1][i], height/9, height/36, true);
            }


            //미사일이미지 만들기
            for(int i=0; i<3; i++){
                imgMissile[i]= BitmapFactory.decodeResource(res, R.drawable.bullet_01+i);
                imgMissile[i]= Bitmap.createScaledBitmap(imgMissile[i], height/10, height/10, true);
            }


            //조이패드이미지 만들기
            imgJoypad= BitmapFactory.decodeResource(res, R.drawable.img_joypad);
            imgJoypad= Bitmap.createScaledBitmap(imgJoypad, height/3, height/3, true);
            jpr= imgJoypad.getWidth()/2;


            //배경이미지 만들기
            imgBack= BitmapFactory.decodeResource(res, R.drawable.back_01+rnd.nextInt(6));
            imgBack= Bitmap.createScaledBitmap(imgBack, width, height, true);


            //적군이미지 만들기
            for(int i=0; i<3; i++){        //캐릭터 종류가 세가지이기 때문!

                //실제로 4개를 만들어야하지만, 하나는 복사하는것이기 때문에 세개만 만들쟈
                for(int j=0; j<3; j++){    //날개짓때문에!
                    imgEnemy[i][j]= BitmapFactory.decodeResource(res, R.drawable.enemy_a_01+(i*3)+j);
                    imgEnemy[i][j]= Bitmap.createScaledBitmap(imgEnemy[i][j], height/9, height/9, true);

                }

                imgEnemy[i][3]= imgEnemy[i][1];

            }


            //플레이어이미지 만들기
            for(int i=0; i<3; i++){     //날개짓 그림 때문에 3개!

                imgPlayer[0][i]= BitmapFactory.decodeResource(res, R.drawable.char_a_01+i);
                imgPlayer[0][i]= Bitmap.createScaledBitmap(imgPlayer[0][i], height/8, height/8, true);
                //휴대폰 화면의 비율이 짧은 쪽을 기반으로 사이즈를 지정하자!

            }
            imgPlayer[0][3]= imgPlayer[0][1];

            for(int i=0; i<3; i++){     //날개짓 그림 때문에 3개!

                imgPlayer[1][i]= BitmapFactory.decodeResource(res, R.drawable.char_b_01+i);
                imgPlayer[1][i]= Bitmap.createScaledBitmap(imgPlayer[1][i], height/8, height/8, true);
                //휴대폰 화면의 비율이 짧은 쪽을 기반으로 사이즈를 지정하자!

            }
            imgPlayer[1][3]= imgPlayer[1][1];

            for(int i=0; i<3; i++){     //날개짓 그림 때문에 3개!

                imgPlayer[2][i]= BitmapFactory.decodeResource(res, R.drawable.char_c_01+i);
                imgPlayer[2][i]= Bitmap.createScaledBitmap(imgPlayer[2][i], height/8, height/8, true);
                //휴대폰 화면의 비율이 짧은 쪽을 기반으로 사이즈를 지정하자!

            }
            imgPlayer[2][3]= imgPlayer[2][1];

        }



        //Resource(Bitmap 객체) 제거하기
        void removeResource(){

            if(sp != null){
                sp.release();
                sp= null;
            }

            imgBomb.recycle();
            imgBomb= null;

            imgProtect.recycle();
            imgProtect= null;       //사실 null은 안해도되지만 해놓자

            imgStrong.recycle();
            imgStrong= null;


            for(int i=0; i<imgItem.length; i++){
                imgItem[i].recycle();
                imgItem[i]= null;

            }


            for(int i=0; i<imgDust.length; i++){
                imgDust[i].recycle();
                imgDust[i]= null;

            }


            for(int i=0; i<imgGauge.length; i++){
                for(int j=0; j<imgGauge[i].length; j++){
                    imgGauge[i][j].recycle();;
                    imgGauge[i][j]= null;

                }
            }


            for(int i=0; i<imgMissile.length; i++){
                imgMissile[i].recycle();
                imgMissile[i]= null;

            }

            imgJoypad.recycle();  imgJoypad= null;
            imgBack.recycle();    imgBack= null;


            //플레이어
            for(int i=0; i<3; i++){
                for(int j=0; j<3; j++){
                    imgPlayer[i][j].recycle();
                    imgPlayer[i][j]= null;
                }

                imgPlayer[i][3]= null;
                //각 줄의 네번째 칸들이 비워지지 않았기 때문에 비워준다!

            }


            //적군
            for(int i=0; i<3; i++) {
                for (int j = 0; j < 3; j++) {
                    imgEnemy[i][j].recycle();
                    imgEnemy[i][j] = null;
                }

                imgEnemy[i][3] = null;

            }
        }


        //2-1 화면에 보이는 모든 객체들 만들어내는 작업
        void makeAll(){

            //적군 만들기
            int p= rnd.nextInt(8-level );
            if(p==0){
                enemies.add(new Enemy(width, height, imgEnemy, me.x, me.y, imgGauge));

            }


            //미사일 만들기
            if(fastTime>0){     //fastItem을 먹었다!
                if(G.isSound) sp.play(sdFireBall, 0.1f, 0.1f, 0, 0, 1);
                missiles.add(new Missile(width, height, imgMissile, me.x, me.y, me.angle, me.kind));
            }else {
                missileGap--;
                if(missileGap==0){
                    if(G.isSound) sp.play(sdFireBall, 0.1f, 0.1f, 0, 0, 1);
                    missiles.add(new Missile(width, height, imgMissile, me.x, me.y, me.angle, me.kind));
                    missileGap=3;
                }
            }


        }


        //2-2 움직이는 모든 작업
        void moveAll(){

            //아이템들 움직이기
            for(int i= items.size()-1; i>=0; i--){
                Item t= items.get(i);

                if( magnetTime>0 && t.kind<2 ){   //모든아이템을 다 끌어오지않고, coin과 gem만 당겨오도록..
                    t.move(me.x, me.y);
                }else {
                    t.move();
                }

                if(t.isDead) items.remove(i);

            }


            //먼지들 움직이기
            for(int i= dusts.size()-1; i>=0; i--){
                Dust t= dusts.get(i);

                t.move();

                if(t.isDead) dusts.remove(i);

            }

            //적군들 움직이기
            for(int i= enemies.size()-1; i>=0; i--){
                Enemy t= enemies.get(i);

                t.move(me.x, me.y);
                if(t.isOut) enemies.remove(i);
                else if(t.isDead){

                    //점수 획득
                    score+=( (t.kind+1) *10 );
                    setTextView();

                    //폭발 효과
                    dusts.add( new Dust(imgDust, t.x, t.y) );

                    //효과음
                    if(G.isSound) sp.play(sdMonDie, 1, 1, 1, 0, 1);

                    //아이템 생성
                    items.add(new Item(width, height, imgItem, t.x, t.y));

                    enemies.remove(i);  //화면 나가던 죽던 제거는 해야함!

                }

            }

            //미사일들 움직이기
            for(int i= missiles.size()-1; i>=0; i--){
                Missile t= missiles.get(i);

                t.move();
                if(t.isDead) missiles.remove(i);

            }


            //플레이어 움직이기
            me.move();

            //배경 움직이기
//            positionBack--;
            positionBack -= width/600;
            //0이 나오면 안되기 때문에!.. 600보다 작은 화면은 존재하지 않는다.
            //어떤 화면이든 600번이면 간다!!

            if(positionBack <= -width)  positionBack += width;
            // = 이 아니라 += 을 쓰는 이유는 1px씩 빠지면 되겠지만, 3px씩 빠진다면 흔들릴 수 있기 때문!

            //아이템 지속 시간 체크 메소드 호출
            checkItemTime();


        }

        //아이템 지속 시간 체크 작업 메소드
        void checkItemTime(){

            if(fastTime>0){     //0보다 커? 아이템을 먹었다!
                fastTime--;     //0이 될 때까지 숫자가 줄어든다.

                if(fastTime==0){
                    me.da= 3;

                }
            }

            if(protectTime>0)   protectTime--;
            if(magnetTime>0)    magnetTime--;
            if(strongTime>0)    strongTime--;

        }


        //아이템의 종류에 따른 작업 메소드
        void actionItem(int kind){

            switch (kind){
                //coin, gem, bomb는 먹으면 바로 발동이 되는 것이 아니므로 나중에 설정하자(숫자만 올라간다!)

                case 0: //coin
                    if(G.isSound) sp.play(sdCoin, 1, 1, 2, 0, 1);
                    coin++;
                    setTextView();

                    break;

                case 1: //gem
                    if(G.isSound) sp.play(sdGem, 1, 1, 3, 0, 1);
                    G.gem++;
                    setTextView();
                    break;

                case 2: //fast
                    if(G.isSound) sp.play(sdProtect, 1, 1, 3, 0, 1);
                    me.da/*회전변화량*/= 9;  //회전변화량을 증가
                    fastTime= 200;  //아이템 지속 시간 적용 --→ 7초정도
                    break;

                case 3: //protect
                    if(G.isSound) sp.play(sdProtect, 0.7f, 0.7f, 4, 0, 1);
                    protectTime= 200;
                    break;

                case 4: //magnet
                    if(G.isSound) sp.play(sdProtect, 1, 1, 3, 0, 1);
                    magnetTime= 200;
                    break;

                case 5: //bomb
                    if(G.isSound) sp.play(sdProtect, 1, 1, 3, 0, 1);
                    bomb++;
                    setTextView();
                    break;

                case 6: //strong (강화미사일)
                    if(G.isSound) sp.play(sdProtect, 1, 1, 3, 0, 1);
                    strongTime= 200;
                    break;


            }

        }


        //2-3 모든 충돌 체크 작업
        void checkCollision(){

            //플레이어와 적군의 충돌
            for(Enemy t: enemies){

                //보호막이 있는가?
                if(protectTime>0){
                    if(Math.pow(me.x - t.x, 2) + Math.pow(me.y - t.y, 2) <= Math.pow(protectRad + t.w, 2)){ //protect에 부딫혔는가?
                        t.isDead= true;
                    }
                }else {
                    if(Math.pow(me.x - t.x, 2) + Math.pow(me.y - t.y, 2) <= Math.pow(me.w + t.w, 2)){
                        t.isDead= true;
                        me.HP--;
                        //피가 깎일때마다 진동효과
                        if(G.isVibrate) vibrator.vibrate(1000);

                        if(me.HP<=0){
                            //Game Over/////////////
                            //효과음
                            if(G.isSound) sp.play(sdChDie, 1, 1.0f, 5, 0, 1);


                            //GameoverActivity 실행!

//                            ((GameActivity)context).startActivity();
//                            ((GameActivity)context).finish();
                            //위 코드는 굉장히 안좋은 코드!(되긴합니다) --→ 직접 건드리지말자!!

                            //본사(GameActivity)에 요청하자!
                            Message msg= new Message();
                            Bundle data=  new Bundle();    //보따리

                            data.putInt("Score", score);
                            data.putInt("Coin" , coin );

                            msg.setData(data);            //자료형 : bundle ←-- bunble에는 몇십개의 Data를 넣을 수 있다.
                            ((GameActivity)context).handler.sendMessage(msg);

                        }
                    }
                }
            }


            //플레이어와 아이템의 충돌
            for(Item t: items){

                if(Math.pow(me.x-t.x, 2)/*x제곱의 거리*/ + Math.pow(me.y-t.y, 2)/*y제곱의 거리*/ <= Math.pow(me.w+t.w,2) ){
                    //아이템의 종류에 따른 동작 수행
                    actionItem(t.kind);
                    t.isDead=true;  //아이템제거
                    break;          //한 번에 하나씩만 하기 위해서 break!
                }

            }



            //미사일과 적군의 충돌
            for(Missile t: missiles){
                for(Enemy et: enemies){
                    if(Math.pow(t.x - et.x, 2) + Math.pow(t.y - et.y,2) <= Math.pow(t.w + et.w, 2)){   //부딫혔다!
                        //적군의 HP를 줄이기 (죽이면 안되고 데미지를 줘야합니다! --→ 미사일은 사라집니당)
                        et.damaged(t.kind+1);

                        if(strongTime==0) t.isDead= true;

                        score+= 5;
                        setTextView();

                        break;  // 미사일하나로 적군 두마리를 죽일 수 있기 때문에 break를 걸어주자!

                    }

                }

            }

        }


        //2-4 화면에 그리는 모든 작업
        void drawAll(Canvas canvas){

            //배경그리기
            canvas.drawBitmap(imgBack, positionBack, 0, null);
            canvas.drawBitmap(imgBack, width+positionBack,0,null);


            //적군들 그리기
            for(Enemy t: enemies){
                canvas.save();
                canvas.rotate(t.angle, t.x, t.y);
                canvas.drawBitmap(t.img, t.x-t.w, t.y-t.h, null);
                canvas.restore();

                if(t.kind>0){
                    canvas.drawBitmap(t.imgG, t.x-t.w, t.y+t.h, null);

                }

            }


            //미사일들 그리기
            for(Missile t: missiles){
                canvas.save();
                canvas.rotate(t.angle, t.x, t.y);
                canvas.drawBitmap( strongTime>0? imgStrong: t.img, t.x-t.w, t.y-t.h, null);
                canvas.restore();
            }


            //아이템들 그리기
            for( Item t: items){
                canvas.drawBitmap(t.img, t.x-t.w, t.y-t.h, null);
            }


            //플레이어 그리기
            canvas.save();
            canvas.rotate(me.angle, me.x, me.y);
            canvas.drawBitmap(me.img, me.x-me.w, me.y-me.w,null);
            canvas.restore();


            //보호막이미지 그리기
            if(protectTime>0){
                protectAng += 15;
                canvas.save();
                canvas.rotate(protectAng, me.x, me.y);
                canvas.drawBitmap(imgProtect, me.x- protectRad, me.y- protectRad, null);
                canvas.restore();

            }


            //먼지들 그리기
            for( Dust t: dusts ){
                for(int i=0; i<6; i++){
                    canvas.drawBitmap(t.img[i], t.x[i]-t.rad[i], t.y[i]- t.rad[i], null);
                }
            }


            //조이패드 그리기
            paint.setAlpha(isJoypad?240:120);  //0~255   절반값인 120정도면 될 듯!
            // isJoypad가 true면(누르면) 240, false면(안누르면) 120!
            canvas.drawBitmap(imgJoypad, jpx- jpr, jpy- jpr, paint);


            //폭탄버튼 그리기
            paint.setAlpha(isBomb? 240: 120);
            canvas.drawBitmap(imgBomb, rectBomb.left, rectBomb.top, paint);

        }


        //터치다운 작업 메소드///////////////////////////////////
        void touchDown(int x, int y){
            //터치다운한 x, y지점이 조이패드인가?
            if(Math.pow(x-jpx, 2)+ Math.pow(y-jpy, 2) <= Math.pow(jpr, 2)) {
                //터치다운한 x, y와 조이패드의 중심좌표(jpx, jpy)사이의 각도 계산
                me.radian= Math.atan2(jpy-y, x-jpx);

                isJoypad= true;
                me.canMove= true;
            }

            //터치다운한 x, y지점이 폭탄버튼의 안에 있는가?
            if(rectBomb.contains(x, y)){
                isBomb= true;

                //폭탄의 수가 1개 이상인가?
                if(bomb>0){
                    bomb--;
                    setTextView();

                    //폭발효과 적용

                    for( Enemy t: enemies ){
                        if(t.wasShow/*화면 안에 들어온 적이 있느냐?*/)   t.isDead= true;

                    }
                }

            }

        }

        void touchUp(int x, int y){
            isJoypad= false;
            me.canMove= false;
            isBomb= false;

        }

        void touchMove(int x, int y){
            //손가락을 떼지않고 움직일 때 마다마다 조이패드의 각도를 계산
            if(isJoypad){
                me.radian= Math.atan2(jpy-y, x-jpx);
            }

        }
        //////////////////////////////////////////////////////


        @Override
        public void run() {

            //초기값 설정
            init();


            Canvas canvas= null;
            while (isRun){
                //1. canvas 얻어오기
                canvas= holder.lockCanvas();

                //2. canvas에 원하는 작업 수행
                try{
                    synchronized (holder){

                        //2-1 객체(화면에 보여질)들 만들기
                        makeAll();

                        //2-2 움직이기
                        moveAll();

                        //2-3 충돌 체크 작업
                        checkCollision();

                        //2-4 그려내는 작업
                        drawAll(canvas);

                    }

                }finally {
                    //3. Horder에게 canvas를 본사 전송(post)
                    holder.unlockCanvasAndPost(canvas);

                }

                if (isWait){
                    try {
                        synchronized (this){
                            wait();     //GameThread 정지!
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }


            }//while

            removeResource();

        }//run method


        //gameThread의 메소드
        void stopThread(){
            isRun= false;
            synchronized (this){
                this.notify();
            }

        }

        void pauseThread(){
            isWait= true;

        }

        void resumeThread(){
//            this.notify();      //notify()와 wait()을 할 때에는 반드시 감싸서 동기(synchronized)를 해야한다!

            isWait= false;      //그냥 notify()만 하면 isWait에서 while문으로인해 다시 멈추기때문에 우선 isWait()을 false로 두고 notify()로 돌린다.

            synchronized (this){    //this를 하는 동안에는 notify()를 건드리지마! 라는 의미
                this.notify();

            }


        }


    }//GameThread class



}//GameView
