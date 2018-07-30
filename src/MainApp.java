import processing.core.PApplet;

public class MainApp extends PApplet {

    public static void main(String[] args) {
        PApplet.main("MainApp");

    }

    @Override
    public void settings() {
//        fullScreen(1);
        size(800,1000);
    }

    private int[][] minefield;
    //0: covered empty
    //1: uncovered empty
    //2: covered mine
    //3: uncovered mine
    //4: last uncovered mine

    private int difficulty;
    private int mineCount;       //total mines in field
    private int minefieldSize;   //n by n tiles
    private float scl;           //(height or width) / minefieldSize;
    private float tileTextSize;

    private int fcenterX; //screenwise field start X
    private int fcenterY; //screenwise field start Y
    private int fwidth ;  //screenwise field width
    private int fheight;  //screenwise field height
    private int fOff;     //offset from top of board to difficulty text

    //unique default values
    private int mousePressedAtX = -1;
    private int mousePressedAtY = -1;
    private int mouseInputX = -3;
    private int mouseInputY = -3;

    //Game state

    private boolean gameOver = false;
    private boolean gameWon = false;

    private float gameStartedMillis = 0;
    private float score = 0;
    private float easyHigh      =  -5;
    private float mediumHigh    =  -5;
    private float hardHigh      =  -5;

    public void setup() {
        fcenterX = width/2;
        fcenterY = height/2;
        fwidth =  (width<height)?width:height;
        fheight = fwidth;
        fOff = fheight / 16;
        reset();
    }

    private void reset() {
        if (difficulty == 0) { //beginner
            mineCount = 10;
            minefieldSize = 9;
        }
        if (difficulty == 1) { //advanced
            mineCount = 40;
            minefieldSize = 16;
        }
        if (difficulty == 2) { //expert
            mineCount = 99;
            minefieldSize = 22; //should be 30 by 16 but meh
        }
        tileTextSize =  fwidth/minefieldSize/2f;
        scl = fheight * 1f / minefieldSize;
        minefield = new int[minefieldSize][minefieldSize];
        int i = 0;
        while (i < mineCount) {
            int x = round(random(minefieldSize - 1));
            int y = round(random(minefieldSize - 1));
            if(getMine(x,y)==0){
                i++;
                setMine(x,y, 2);
            }
        }
        gameOver = false;
        gameWon = false;
        score = 0;
        gameStartedMillis = millis();
    }

    public void draw() {
        if(!gameOver){
            score = millis() - gameStartedMillis;
        }
        background(0);
        displayHud();
        if (width<height) {
            translate(0, (height-fheight)/2);
        } else {
            translate((width-fwidth)/2, 0);
        }
        tryRevealClickedMines();
        checkWin();
        displayField();
    }

    public void mousePressed() {
        mousePressedAtX = round(map(mouseX-scl/2, fcenterX-fwidth/2, fcenterX+fwidth/2, 0, minefieldSize));
        mousePressedAtY = round(map(mouseY-scl/2, fcenterY-fheight/2, fcenterY+fheight/2, 0, minefieldSize));
    }

    public void mouseReleased() {
        //difficulty control
        if(mouseY < fcenterY - fheight/2 - 15){

        int chosenDifficulty = floor(map(mouseX, 0, width, 0 ,3));

            if (chosenDifficulty == 0) {
                difficulty = 0;
                reset();
            }
            if (chosenDifficulty == 1) {
                difficulty = 1;
                reset();
            }
            if (chosenDifficulty == 2) {
                difficulty = 2;
                reset();
            }
        }

        //must ensure the same tile was pressed AND released to make input cancellable by dragging the mouse/finger elsewhere
        float scl = fheight * 1f / minefieldSize;
        int mouseReleasedAtX = round(map(mouseX-scl/2, fcenterX-fwidth/2, fcenterX+fwidth/2, 0, minefieldSize));
        int mouseReleasedAtY = round(map(mouseY-scl/2, fcenterY-fheight/2, fcenterY+fheight/2, 0, minefieldSize));
        if (mousePressedAtX == mouseReleasedAtX && mousePressedAtY == mouseReleasedAtY) {
            mouseInputX = mouseReleasedAtX;
            mouseInputY = mouseReleasedAtY;
        }
        mousePressedAtX = -1;
        mousePressedAtY = -1;
    }

    private void tryRevealClickedMines() {
        if (mouseInputX != -3 && mouseInputY != -3) {
            if (gameOver) {
                reset();
            } else {
                int x = mouseInputX;
                int y = mouseInputY;
                //                println(x + ":" + y);
                if (x >= 0 && y >= 0 && x < minefieldSize && y < minefieldSize) {
                    int val = getMine(x,y);
                    if (val == 0) {             //0: covered empty
                        tryUncover(x, y);
                    } else if (val == 2) {      //2: covered mine
                        setMine(x,y, 3);    //3: uncovered mine
                        gameOver = true;
                    }
                }
            }
        }
        mouseInputX = -3;
        mouseInputY = -3;
    }

    private void tryUncover(int x, int y) {
        if (getMine(x,y) != 0){
            return;
        }
        //if x,y is not a mine, change it from covered empty to uncovered empty
        setMine(x,y, 1);
        if (neighbourMines(x, y) > 0){
            return;
        }
        //if x,y has no mine neighbours, uncover all empty neighbours in the same way as x,y was uncovered
        tryUncover(x-1, y);        //left
        tryUncover(x-1, y+1);   //left down
        tryUncover(x-1, y-1);   //left up
        tryUncover(x+1, y);        //right
        tryUncover(x+1, y+1);   //right down
        tryUncover(x+1, y-1);   //right up
        tryUncover(x, y-1);        //up
        tryUncover(x, y+1);        //down
    }

    private int neighbourMines(int x, int y) {
        int result = 0;
        if (isMine(x-1,y))             result++;
        if (isMine(x-1,y-1))        result++;
        if (isMine(x, y-1))            result++;
        if (isMine(x+ 1,y-1))       result++;
        if (isMine(x+ 1,y))            result++;
        if (isMine(x+1, y+1))       result++;
        if (isMine(x, y + 1))          result++;
        if (isMine(x-1, y+1))       result++;
        return result;
    }

    private boolean isMine(int x, int y) {
        return getMine(x,y) >= 2;
    }

    private int getMine(int x, int y) {
        if (x >= 0 && y >= 0 && x < minefieldSize && y < minefieldSize){
            return minefield[x][y];
        }
        return -1; //not a valid cell state
    }

    private void setMine(int x, int y, int val){
        if (x >= 0 && y >= 0 && x < minefieldSize && y < minefieldSize){
            minefield[x][y] = val;
        }
    }

    private void checkWin() {
        boolean isWin = true;
        for (int x = 0; x < minefieldSize; x++) {
            for (int y = 0; y < minefieldSize; y++) {
                if (getMine(x,y)==0) {
                    isWin = false;
                }
            }
        }
        if (isWin) {
            if(difficulty==0){

                if (easyHigh == -5 || score < easyHigh){
                    easyHigh = score;
                }
            }
            if(difficulty==1){

                if (easyHigh ==  -5  || score < mediumHigh){
                    mediumHigh = score;
                }
            }
            if(difficulty==2){

                if (hardHigh ==  -5  || score < hardHigh){
                    hardHigh = score;
                }
            }
            gameOver = true;
            gameWon = true;
        }
    }

    private void displayField() {
        textAlign(CENTER, CENTER);
        textSize(tileTextSize);
        float scl = fheight * 1f / minefieldSize;
        for (int x = 0; x < minefieldSize; x++) {
            for (int y = 0; y < minefieldSize; y++) {
                rectMode(CENTER);
                int val = getMine(x,y);
                stroke(0);
                if (val == 0) {    //0: covered empty
                    fill(150);
                }
                if (val == 1) {    //1: uncovered empty
                    fill(255);
                }
                if (val == 2) {    //2: covered mine
                    if (!gameOver) {
                        fill(150);
                    } else {
                        fill(150, 0, 0); //darker than uncovered mine to differentiate at gameover
                    }
                }
                if (val > 2) {    //3: uncovered mine
                    fill(255, 0, 0);
                }
                rect((x * scl)+scl/2, (y * scl)+scl/2, scl, scl);
                if (val == 1) {
                    int neighbourMineCount = neighbourMines(x, y);
                    if (neighbourMineCount == 1) fill(0, 0, 255);
                    if (neighbourMineCount == 2) fill(0, 200, 0);
                    if (neighbourMineCount == 3) fill(255, 0, 0);
                    if (neighbourMineCount == 4) fill(200, 0, 0);
                    if (neighbourMineCount == 5) fill(128, 0, 0);
                    if (neighbourMineCount == 6) fill(64, 224, 208);
                    if (neighbourMineCount == 7) fill(0);
                    if (neighbourMineCount >= 8) fill(150);
                    if (neighbourMineCount>0) {
                        rectMode(CORNER);
                        text(neighbourMineCount+"", (x*scl), (y*scl), scl, scl);
                    }
                }
            }
        }
        if (gameWon) {
            rectMode(CENTER);
            noStroke();
            fill(0,255,0,100);
            rect(fwidth/2, fheight/2, width, height/6);
            rectMode(CENTER);
            textSize(60);
            fill(0);
            text("VICTORY!", fwidth/2, fheight/2);
        } else if (gameOver) {
            rectMode(CENTER);
            noStroke();
            fill(255,0,0,100);
            rect(fwidth/2, fheight/2, width, height/6);
            rectMode(CENTER);
            textSize(60);
            fill(0);
            text("GAME OVER", fwidth/2, fheight/2);
        }
    }

    private void displayHud() {
        pushMatrix();

        if(height < width){
            return;
        }

        translate(width/6, 0);

        if (difficulty==0){
            fill(255);
        }
        else{
            fill(150);
        }
        textSize(20);
        textAlign(CENTER, CENTER);
        text("easy", 0, 40);
        if(easyHigh !=  -5 ){
            textSize(16);
            fill(150);
            text(String.format("%.2f", easyHigh/1000), 0, 20);
        }
        if(difficulty == 0){
            if(score < easyHigh){
                fill(255);
            }else{
                fill(150);
            }
            textSize(16);
            text(String.format("%.2f", score/1000), 0, 65);
        }

        translate(width/3, 0);

        if (difficulty==1)fill(255);
        else fill(150);
        textSize(20);
        textAlign(CENTER, CENTER);
        text("medium", 0, 40);
        if(mediumHigh !=  -5 ){
            textSize(16);
            fill(150);
            text(String.format("%.2f", mediumHigh/1000), 0, 20);
        }
        if(difficulty == 1){

            textSize(16);
            if(score < mediumHigh){
                fill(255);
            }else{
                fill(150);
            }
            text(String.format("%.2f", score/1000), 0, 65);
        }

        translate(width/3, 0);

        //stroke(255);
        //fill(0);
        //rect(0, 0, width/3, fstartY-fOff);
        if (difficulty==2){
            fill(255);
        }
        else{
            fill(150);
        }
        textSize(20);
        textAlign(CENTER, CENTER);
        text("hard", 0, 40);
        if(hardHigh !=  -5 ){
            textSize(16);
            fill(150);
            text(String.format("%.2f", hardHigh/1000), 0, 20);
        }
        if(difficulty == 2){

            if(score < hardHigh){
                fill(255);
            }else{
                fill(150);
            }
            textSize(16);
            text(String.format("%.2f", score/1000), 0, 65);
        }
        popMatrix();
    }
}