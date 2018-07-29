import processing.core.PApplet;

public class MainApp extends PApplet {

    public static void main(String[] args) {
        PApplet.main("MainApp");

    }

    @Override
    public void settings() {
//        fullScreen(1);
        size(600,800);
    }

    int mineCount;    //total mines in field
    int minefieldSize; //n by n tiles
    float scl;         //(height or width) / minefieldSize;
    int difficulty;

    //0: covered empty
    //1: uncovered empty
    //2: covered mine
    //3: uncovered mine
    //4: last uncovered mine
    int[][] minefield;

    boolean gameOver = false;
    boolean gameWon = false;
    float textSize = 0;

    int fOff;
    int fstartX; //screenwise field start X
    int fstartY; //screenwise field start Y
    int fwidth ; //screenwise field width
    int fheight; //screenwise field height

    //unique default values
    int mousePressedAtX = -1;
    int mousePressedAtY = -1;
    int mouseReleasedAtX = -2;
    int mouseReleasedAtY = -2;
    int mouseInputX = -3;
    int mouseInputY = -3;

    float gameStartedMillis = 0;
    float easyHigh      = 999999999;
    float mediumHigh    = 999999999;
    float hardHigh      = 999999999;

    public void setup() {
        fwidth =  (width<height)?width:height;
        fheight = fwidth;
        fstartX = (width>height)?(width-fwidth)/2:0;
        fstartY = (width>height)?0:(height-fheight)/2;
        fOff = fheight / 16;
        reset();
    }

    private void reset() {
        if (difficulty == 0) { //beginner
            mineCount = 10;
            minefieldSize = 9;
            textSize = 40;
        }
        if (difficulty == 1) { //advanced
            mineCount = 40;
            minefieldSize = 16;
            textSize = 25;
        }
        if (difficulty == 2) { //expert
            mineCount = 99;
            minefieldSize = 22; //should be 30 by 16 but meh
            textSize = 20;
        }

        scl = fheight * 1f / minefieldSize;
        minefield = new int[minefieldSize][minefieldSize];
        int i = 0;
        while (i++ < mineCount) {
            int x = round(random(minefieldSize - 1));
            int y = round(random(minefieldSize - 1));
            minefield[x][y] = 2;
        }
        gameOver = false;
        gameWon = false;
        gameStartedMillis = millis();
    }

    public void draw() {
        background(0);
        gui();
        if (width<height) {
            translate(0, (height-fheight)/2);
        } else {
            translate((width-fwidth)/2, 0);
        }
        input();
        checkWin();
        display();
    }

    private void checkWin() {
        boolean isWin = true;
        for (int x = 0; x < minefieldSize; x++) {
            for (int y = 0; y < minefieldSize; y++) {
                if (minefield[x][y]==0) {
                    isWin = false;
                }
            }
        }
        if (isWin) {
            if(difficulty==0){
                float score = millis() - gameStartedMillis;
                if (easyHigh == 999999999 || score < easyHigh){
                    easyHigh = score;
                }
            }
            if(difficulty==1){
                float score = millis() - gameStartedMillis;
                if (easyHigh == 999999999 || score < mediumHigh){
                    mediumHigh = score;
                }
            }
            if(difficulty==2){
                float score = millis() - gameStartedMillis;
                if (hardHigh == 999999999 || score < hardHigh){
                    hardHigh = score;
                }
            }
            gameOver = true;
            gameWon = true;
        }
    }

    void gui() {
        pushMatrix();
        if (height>width) {
            //stroke(255);
            //fill(0);
            //rect(0, 0, width/3, fstartY-fOff);
            if (difficulty==0)fill(255);
            else fill(150);
            textSize(40);
            textAlign(CENTER, CENTER);
            text("easy", 0, 0, width/3, fstartY-fOff);
            if(easyHigh != 999999999){
                textSize(20);
                fill(150);
                text(String.format("%.2f", easyHigh/1000), 0, 0, width/3, fstartY+fOff);
            }
            translate(width/3, 0);

            //stroke(255);
            //fill(0);
            //rect(0, 0, width/3, fstartY-fOff);
            if (difficulty==1)fill(255);
            else fill(150);
            textSize(40);
            textAlign(CENTER, CENTER);
            text("medium", 0, 0, width/3, fstartY-fOff);
            if(mediumHigh != 999999999){
                fill(150);
                textSize(20);
                text(String.format("%.2f", mediumHigh/1000), 0, 0, width/3, fstartY+fOff);
            }
            translate(width/3, 0);

            //stroke(255);
            //fill(0);
            //rect(0, 0, width/3, fstartY-fOff);
            if (difficulty==2)fill(255);
            else fill(150);
            textSize(40);
            textAlign(CENTER, CENTER);
            text("hard", 0, 0, width/3, fstartY-fOff);
            if(hardHigh!= 999999999){
                fill(150);
                textSize(20);
                text(String.format("%.2f", hardHigh/1000), 0, 0, width/3, fstartY+fOff);
            }
        }
        popMatrix();
        float score = millis() - gameStartedMillis;
        textSize(20);
        text(String.format("%.2f", score/1000), 50, height-50);
    }

    public void mousePressed() {
        mousePressedAtX = round((mouseX-fstartX - scl / 2) / scl);
        mousePressedAtY = round((mouseY-fstartY - scl / 2) / scl);
    }

    public void mouseReleased() {
        //difficulty control
        if (mouseX > 0 && mouseX < width / 3 && mouseY > 0 && mouseY < fstartY-fOff) {
            difficulty = 0;
            reset();
        } else if (mouseX > width / 3 && mouseX < width / 3+width / 3 && mouseY > 0 && mouseY < fstartY-fOff) {
            difficulty = 1;
            reset();
        } else if (mouseX > width / 3 + width / 3 && mouseX < width  && mouseY > 0 && mouseY < fstartY-fOff) {
            difficulty = 2;
            reset();
        }

        //minefield input - must ensure the same tile was pressed AND released to make input cancellable by dragging the mouse/finger elsewhere
        float scl = fheight * 1f / minefieldSize;
        mouseReleasedAtX = round(((mouseX-fstartX) - scl / 2) / scl);
        mouseReleasedAtY = round(((mouseY-fstartY) - scl / 2) / scl);
        if (mousePressedAtX == mouseReleasedAtX && mousePressedAtY == mouseReleasedAtY) {
            mouseInputX = mouseReleasedAtX;
            mouseInputY = mouseReleasedAtY;
        }
        mousePressedAtX = -1;
        mousePressedAtY = -1;
    }

    private void input() {
        if (mouseInputX != -3 && mouseInputY != -3) {
            if (gameOver) {
                reset();
            } else {
                int x = mouseInputX;
                int y = mouseInputY;
                //                println(x + ":" + y);
                if (x >= 0 && y >= 0 && x < minefieldSize && y < minefieldSize) {
                    int val = minefield[x][y];
                    if (val == 0) {             //0: covered empty
                        tryUncover(x, y);
                    } else if (val == 2) {      //2: covered mine
                        minefield[x][y] = 3;    //3: uncovered mine
                        gameOver = true;
                    }
                }
            }
        }
        mouseInputX = -3;
        mouseInputY = -3;
    }

    private void tryUncover(int x, int y) {
        if (minefield[x][y] > 0){
            return;
        }
        //if x,y is not a mine, change it from covered empty to uncovered empty
        minefield[x][y] = 1;

        if (neighbourMines(x, y) > 0){
            return;
        }
        //if x,y has no mine neighbours, uncover all empty neighbours in the same way as x,y was uncovered
        if (x - 1 >= 0){
            tryUncover(x-1, y);                                 //left
            if (y + 1 < minefieldSize) tryUncover(x-1, y+1); //left down
            if (y - 1 >= 0)            tryUncover(x-1, y-1); //left up
        }
        if (x + 1 < minefieldSize){
            tryUncover(x+1, y);                                 //right
            if (y + 1 < minefieldSize) tryUncover(x+1, y+1); //right down
            if (y - 1 >= 0)            tryUncover(x+1, y-1); //right up
        }
        if (y - 1 >= 0)            tryUncover(x, y-1);          //up
        if (y + 1 < minefieldSize) tryUncover(x, y+1);          //down


    }

    private int neighbourMines(int x, int y) {
        int result = 0;
        int nx = x - 1;
        int ny = y;
        if (nx >= 0 && ny >= 0 && nx < minefieldSize && ny < minefieldSize && isMine(nx, ny)) {
            result++;
        }
        nx = x - 1;
        ny = y - 1;
        if (nx >= 0 && ny >= 0 && nx < minefieldSize && ny < minefieldSize && isMine(nx, ny)) {
            result++;
        }
        nx = x;
        ny = y - 1;
        if (nx >= 0 && ny >= 0 && nx < minefieldSize && ny < minefieldSize && isMine(nx, ny)) {
            result++;
        }
        nx = x + 1;
        ny = y - 1;
        if (nx >= 0 && ny >= 0 && nx < minefieldSize && ny < minefieldSize && isMine(nx, ny)) {
            result++;
        }
        nx = x + 1;
        ny = y;
        if (nx >= 0 && ny >= 0 && nx < minefieldSize && ny < minefieldSize && isMine(nx, ny)) {
            result++;
        }
        nx = x + 1;
        ny = y + 1;
        if (nx >= 0 && ny >= 0 && nx < minefieldSize && ny < minefieldSize && isMine(nx, ny)) {
            result++;
        }
        nx = x;
        ny = y + 1;
        if (nx >= 0 && ny >= 0 && nx < minefieldSize && ny < minefieldSize && isMine(nx, ny)) {
            result++;
        }
        nx = x - 1;
        ny = y + 1;
        if (nx >= 0 && ny >= 0 && nx < minefieldSize && ny < minefieldSize && isMine(nx, ny)) {
            result++;
        }
        return result;
    }

    private boolean isMine(int x, int y) {
        return minefield[x][y] >= 2;
    }

    private void display() {
        float scl = fheight * 1f / minefieldSize;
        for (int x = 0; x < minefieldSize; x++) {
            for (int y = 0; y < minefieldSize; y++) {
                int val = minefield[x][y];
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
                        fill(150, 0, 0);
                    }
                }
                if (val > 2) {    //3: uncovered mine
                    fill(255, 0, 0);
                }
                rect(x * scl, y * scl, scl, scl);
                if (val == 1) {
                    textSize(textSize);
                    textAlign(CENTER, CENTER);
                    int neighbourMineCount = neighbourMines(x, y);
                    if (neighbourMineCount == 1) {
                        fill(0, 0, 200);
                    }
                    if (neighbourMineCount == 2) {
                        fill(0, 200, 0);
                    }
                    if (neighbourMineCount > 2) {
                        fill(200, 0, 0);
                    }
                    if (neighbourMineCount>0) {
                        text(neighbourMineCount+"", x*scl, y*scl, scl, scl);
                    }
                }
            }
        }
        if (gameWon) {
            textSize(60);
            textAlign(CENTER, CENTER);
            fill(0);
            text("Congratulations\nYOU WIN!", fwidth/2, fheight/2);
        } else if (gameOver) {
            textSize(60);
            textAlign(CENTER, CENTER);
            fill(0);
            text("GAME OVER", fwidth/2, fheight/2);
        }
    }
}