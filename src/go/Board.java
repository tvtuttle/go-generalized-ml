/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.*;
import java.util.Hashtable;

/**
 * inspired heavily by levenick code (i.e. lots of copying)
 * @author tvtuttle
 */
public class Board implements Cloneable{
    int n;
    int numTurns;
    Controller control;
    int[][] board;
    int passCount;
    int whoseTurn;
    Move lastMove;
    int blackCaptures; // pieces captured by black 
    int whiteCaptures; // pieces captured by white
    // final ints
    public static final int BLACK = -1, WHITE = 1, EMPTY = 0, FOO = 100;
//    final int BLACK_DEAD = 11, WHITE_DEAD=-11;
//    final int BLACK_TERR=2, WHITE_TERR=-2, SEKI_TERR=100, BUSTED=99;
    
    Group[][] groups;
    Group[][] regions;
    GroupList capturedList;
    GroupList theGroupList;
    GroupList theRegionList;
    Hashtable ht; //used to keep track of previous moves, for repeat(m)
    ScoreCounterSimple theScoreCounter;
    boolean suppressPrint;

// display constants
    int squareSize;
    int top;
    int left;
    int width;
    int height;
    
    public Board(){
        n = 7;
        numTurns = 0;
    }
    
    public Board(int size){
        n = size;
        numTurns = 0;
        board = new int[n][n];
//        board[1][1]=-1;
        passCount = 0;
        whoseTurn = -1;
        capturedList = new GroupList();
//        printBoard();
        ht = new Hashtable();
        suppressPrint = false;
        theGroupList = new GroupList();
    }
    
    public void paint(Graphics g){
//        g.fillOval(100,100,100,100);
        Rectangle r = control.getRect();
        g.setColor(Color.GREEN);
        g.fillRect(r.x, r.y, r.width+1, r.height+1);
        width = r.width;
        height = r.height;
        squareSize = width / n;
        top = r.y + squareSize/2;
        left = r.x + squareSize/2;
        g.setColor(Color.BLACK);
        for (int i = 0; i < n; i++) {
            //vert
            g.drawLine(left + i*squareSize, top, left + i * squareSize, top + squareSize * (n-1));
            //hor
            g.drawLine(left, top+i*squareSize, left+squareSize*(n-1), top+i*squareSize);
        }
        
        for (int row=0; row<n; row++){
            for (int col=0; col<n; col++){
                if (board[col][row] != EMPTY){
                    drawPiece(g, col, row);
                }
            }
        }
    }
    
    void drawPiece(Graphics g, int col, int row){
        int x = left+squareSize*col;
        int y = top+squareSize*row;
        int r = squareSize/2-3;
        
        Color c = null;
        int size = 0;
        boolean dead = false;
        switch(board[col][row]){
            case BLACK:
                c = Color.BLACK;
                size = r*2; break;
            case WHITE:
                c = Color.WHITE;
                size = r*2; break;
            default:
                c = Color.BLUE;
                size = r;
                break;
        }
        
        GoPiece p = new GoPiece(x, y, r, c);
        p.paint(g);
    }

    boolean gameOver() {
//        return (numTurns > 100);
        return (passCount >= 2);
    }

    void playerMove(Move a) {
        System.out.println("The player has moved!");
        numTurns ++;
    }

    void advMove(Move b) {
        System.out.println("The opponent has moved!");
        numTurns ++;
    }

    int getWinner() {
        System.out.println("The winner is...");
        if (Math.random() > 0.5){
            return 1;
        }
        else
            return 2;
    }
    
    public void setController(Controller c){
        control = c; 
    }
    public void setFoo(Move m){
        board[m.getX()][m.getY()]=FOO;
    }
    public void reset(){
        board = new int[n][n];
        numTurns = 0;
        passCount = 0;
        whoseTurn = -1;
        capturedList = new GroupList();
        blackCaptures = 0;
        whiteCaptures = 0;
        ht = new Hashtable();
    }
    public void handleClicked(int x, int y){
        int t = top -squareSize/2;
        int l = left-squareSize/2;
        int h = squareSize*(n+1);
        int w = h;
        if (x>=l && x<=l+w && y>=t && y<= t+h){
            tryMove(new Move((x-l)/squareSize, (y-t)/squareSize));
        }
//        if (gameOver()){
//            resolveGame();
//        }
        
    }
    
    void resolveGame(){
        // determine and report the score, then reset the board
        ScoreCounterSimple sc = new ScoreCounterSimple(this);
        int out = sc.countScore();
        if (out >0 && !suppressPrint){
            System.out.println("White wins by " + out);
        }
        else if (out < 0 && !suppressPrint){
            System.out.println("Black wins by " + Math.abs(out));
        }
        else if (out == 0 && !suppressPrint){
            System.out.println("Jigo");
        }
        reset();
    }
    
    public void tryMove(Move m){
        if (m.isPass()){
//            System.out.println("passing");
            passCount++;
            switchTurn();
        }
        else if (!legalMove(m)){
            System.out.println("illegal");
            return;
        }
        else {
            lastMove = m;
            int numCaptures = makeMoveNoKo(m);
            passCount = 0;
            if (BLACK == whoseTurn){ //reversed b/c switchturn already
                whiteCaptures += numCaptures;
            }
            else {
                blackCaptures += numCaptures;
            }
        }
//        System.out.println("blackCaptures = " + blackCaptures);
//        System.out.println("whiteCaptures = " + whiteCaptures);
        ht.put(toString(), this);//the string is a key to the object
//        if (gameOver()){
//            resolveGame();
//        }
    }
    public void switchTurn(){
//        System.out.println("turn swithc");
        whoseTurn = -1*whoseTurn;
        control.played = true;
    }
    
    boolean legalMove(Move m){
        if (!onBoard(m)){
//            System.out.println("not on board");
            return false;
        }
        if (board[m.getX()][m.getY()] != EMPTY){
//            System.out.println("space is taken");
            return false;
        }
        if (repeat(m)){
//            System.out.println("repeat move");
            return false;
        }
        if (suicide(m)){
//            System.out.println("suicide");
            return false;
        }
        return true;
    }
    boolean onBoard(Move m){
        int x = m.getX();
        int y = m.getY();
        return (x >=0 && x < n && y >= 0 && y < n);
    }
    // checks a single dimension, row or col
    boolean on(int i){
        return (i>=0&&i<n);
    }
    boolean repeat(Move m){
        Board tempBoard = (Board) clone();
        tempBoard.board[m.getX()][m.getY()] = whoseTurn;
        tempBoard.removeCaptures(m);
        if (ht.get(tempBoard.toString()) != null){
            return true;
        }
        return false;
    }
    boolean suicide(Move m){
        Board tempBoard = (Board) clone();
        tempBoard.board[m.getX()][m.getY()] = whoseTurn;
        tempBoard.removeCaptures(m);
//        System.out.println(tempBoard.groups[0][0]);
        if (tempBoard.numLibs(m) == 0){
//            System.out.println("that's suicide");
            return true;
        }
        return false;
    }
    int makeMoveNoKo(Move m){
        board[m.getX()][m.getY()] = whoseTurn;
        generateGroups();
        int numCaptures = removeCaptures(m);
        switchTurn();
        return numCaptures;
    }
    int removeCaptures(Move m){
        // for each connected intersection
        // if (enemy group there)
        // if (liberties = 0)
        // remove
        int sumPris = 0;
//        System.out.println("1");
        sumPris += removeIf(m, 0, 1);
//        System.out.println("2");
        sumPris += removeIf(m, 0, -1);
//        System.out.println("3");
        sumPris += removeIf(m, 1, 0);
//        System.out.println("4!");
        sumPris += removeIf(m, -1, 0);
        generateGroups();
//        System.out.println(capturedList.size());
        return sumPris;
    }
    int removeIf(Move m, int dx, int dy){
        int numPris = 0;
        int col = m.getX() + dx;
        int row = m.getY() + dy;
        
        if (!on(row) || !on(col)){
            return 0;
        }
        if (enemyAt(col,row,board[m.getX()][m.getY()])
                && groups[col][row].getLiberties()==0){
            numPris = removeGroup(col, row);
        }
        return numPris;
    }
    
    boolean enemyAt(int col, int row, int color){
        if (groups[col][row]==null){
            return false;
        }
        if (groups[col][row].getColor() != color){
            return true;
        }
        return false;
    }
    
    
    int removeGroup(int col, int row){
        Group thisGroup=groups[col][row];
//        System.out.println(thisGroup);
        int numPris = 0;
        capturedList.add(thisGroup);
//        System.out.println(capturedList.toString());
        MoveList stoneList = thisGroup.getStoneList();
        for (int i = 0; i < stoneList.size(); i ++){
            Move thisPlace = stoneList.get(i);
            int c = thisPlace.getX();
            int r = thisPlace.getY();
            board[c][r] = EMPTY;
            groups[c][r] = null;
            numPris++;
        }
        theGroupList.countLiberties();
        return numPris;
    }
    int removeGroup(Group thisGroup){
        int numPris = 0;
        capturedList.add(thisGroup);
        MoveList stoneList = thisGroup.getStoneList();
        for (int i = 0; i < stoneList.size(); i ++){
            Move thisPlace = stoneList.get(i);
            int c = thisPlace.getX();
            int r = thisPlace.getY();
            board[c][r] = EMPTY;
            groups[c][r] = null;
            numPris++;
        }
        theGroupList.countLiberties();
        return numPris;
    }
    //this is IMPORTANT!!!! 
    GroupList generateGroups(){
        theGroupList = new GroupList();
        groups = new Group[n][n];
        
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (board[col][row] != EMPTY){
                    if (notInGroup(groups, col, row)){
                        groups[col][row] = new Group(col, row, this);
                        theGroupList.add(groups[col][row]);
                        addOtherConnectedStones(groups, col, row);
                    }
                }
            }
        }
        theGroupList.countLiberties();
        return theGroupList;
    }
    // this too
    // a region only contains empty stones
    GroupList generateRegions(){
        theRegionList = new GroupList();
        regions = new Group[n][n];
        
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (board[col][row] == EMPTY){
                    if (notInGroup(regions, col, row)){
                        regions[col][row] = new Group(col, row, this);
                        theRegionList.add(regions[col][row]);
                        addOtherConnectedStones(regions, col, row);
                    }
                }
            }
        }
        return theRegionList;
    }
    boolean notInGroup(Group[][] groupPtrs, int col, int row){
        return groupPtrs[col][row]==null;
    }
    void addOtherConnectedStones(Group[][] groupPtrs, int col, int row){
        addIf(groupPtrs, groupPtrs[col][row], col, row+1);
        addIf(groupPtrs, groupPtrs[col][row], col, row-1);
        addIf(groupPtrs, groupPtrs[col][row], col+1, row);
        addIf(groupPtrs, groupPtrs[col][row], col-1, row);
    }
    void addIf(Group[][] groupPtrs, Group mainGroup, int col, int row){
        if (!on(row) || !on(col)){
            return;
        }
        if (mainGroup.sameColor(board[col][row]) && notInGroup(groupPtrs, col, row)){
            mainGroup.addStone(col, row);
            groupPtrs[col][row] = mainGroup;
            addOtherConnectedStones(groupPtrs, col, row);
        }
    }
    
    
    public Object clone(){
        Board nuBoard = null;
        try{
            nuBoard = (Board) super.clone();
        } catch(Exception e){
            System.out.println("Ugh" + e);
        }
        nuBoard.board = new int[n][n];
        nuBoard.theGroupList = null;
        nuBoard.groups = new Group[n][n];
        nuBoard.theRegionList = null;
        nuBoard.regions = new Group[n][n];
        nuBoard.ht = (Hashtable) ht.clone();
        
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                nuBoard.board[col][row]=board[col][row];
            }
        }
        nuBoard.generateGroups();
        nuBoard.generateRegions();
        nuBoard.capturedList = new GroupList();
        nuBoard.whoseTurn = whoseTurn;
        nuBoard.passCount = passCount;
        nuBoard.suppressPrint = suppressPrint;
        return nuBoard;
    }
    
    public Board myClone(){
        Board tempBoard = null;
        try {
            tempBoard = (Board) this.clone();
        } catch (Exception e){
            System.out.println("oops" + e);
        }
        return tempBoard;
    }
    
    int getSize(){
        return n;
    }
    void setEmpty(Move m){
        board[m.getX()][m.getY()] = EMPTY;
    }
    int numLibs(Move m){
//        System.out.println("finding liberties");
        return groups[m.getX()][m.getY()].getLiberties();
    }
    void pass(){
        tryMove(new Move(-1,-1));
    }
    int getPassCount(){
        return passCount;
    }
    int getBlackCaptures(){
        return blackCaptures;
    }
    void setBlackCaptures(int i){
        blackCaptures = i;
    }
    int getWhiteCaptures(){
        return whiteCaptures;
    }
    void setWhiteCaptures(int i){
        whiteCaptures = i;
    }
    
    // the score finding algorithm
    // adopted from JRLScoreCounter class
    public void countScore(){
        theScoreCounter = new ScoreCounterSimple(this);
        theScoreCounter.countScore();
    }
//    public int getScore(){
//        theScoreCounter = new ScoreCounterSimple(this);
//        return theScoreCounter.countEm();
//    }
    public GroupList getCapturedList(){
//        System.out.println(capturedList);
//        System.out.println("end");
        return capturedList;
    }
    
    // stuff to calculate score at end of game
    // there are many algorithms
    // let's try chinese area scoring:
    // at end, remove all white stones and fill black territory with stones
    // then, count black stones and compare to half of total positions
//    public int countScore(){
//        // remove dead stones of both sides from board
//        Board tempBoard = (Board) clone();
//        tempBoard = removeDeadStones(tempBoard);
//    }
    // based off levenick lifereader
    // for each group, if 2 or more eyes, live, if 1, check shape
//    Board removeDeadStones(Board b){
//        for (Group g: b.theGroupList) {
//            if (checkEyes(g)){
//                g.setStatus(Group.ALIVE);
//            }
//            else {
//                g.setStatus(Group.DEAD);
//            }
//        }
//        for (int i = 0; i < b.getSize(); i++) {
//            for (int j = 0; j < b.getSize(); j++) {
//                if (b.groups[i][j].getStatus() == Group.DEAD){
//                    b.board[i][j] = EMPTY;
//                }
//            }
//        }
//        return b;
//    }
//    
//    boolean checkEyes(Group g){
//        // checks that no one region completely fills (??)
//        GroupList theSurroundList = new GroupList();
//    }
//    
//    
    // for the score counter
    boolean whiteStoneAdjacent(Move m){
        return stoneAdjacent(m, WHITE);
    }
    boolean blackStoneAdjacent(Move m){
        return stoneAdjacent(m, BLACK);
    }
    boolean stoneAdjacent(Move m, int color){
        return adjacent(m, 0, 1, color)
                || adjacent(m, 0, -1, color)
                || adjacent(m, 1, 0, color)
                || adjacent(m, -1, 0, color);
    }
    boolean adjacent(Move m, int dx, int dy, int color){
        int row = m.getY()+dy;
        int col = m.getX()+dx;
        if (!on(row) || !on(col)){
            return false;
        }
        return board[col][row] == color;
    }
    
    public String toString(){
        String out = "";
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                out += (board[i][j])+"\t";
            }
            out+=("\n");
        }
        return out;
    }
    
    void printBoard(){
        System.out.println(this.toString());
    }
    
    MoveList getLegalMoves(){
        MoveList out = new MoveList();
        // pass is always legal
        out.add(new Move(-1,-1));
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Move m = new Move(i, j);
                if (legalMove(m)){
                    out.add(m);
                }
            }
        }
        return out;
    }
    
    int getNumPieces(int player){
        int out = 0;
        if (player == WHITE){
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (board[i][j] == WHITE){
                        out ++;
                    }
                }
            }
        }
        else if (player == BLACK){
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (board[i][j] == BLACK){
                        out ++;
                    }
                }
            }
        }
        return out;
    }
    int getNumCenterPieces(int player){
//        System.out.println(player);
//        System.out.println(board[1][1]);
        int out = 0;
        for (int i = 1; i < n-1; i++) {
//            System.out.println("huh");
            for (int j = 1; j < n-1; j++) {
                if (board[i][j] == player){
                    out ++;
                }
            }
        }
//        System.out.println(out);
        return out;
    }
    
    public int getNumGroups(int player){
        int out = 0;
        for (Group g : theGroupList) {
            if (player == g.getColor()){
                out ++;
            }
        }
        return out;
    }
    
    public int getLargestGroup(int player){
        int out = 0;
        for (Group g : theGroupList) {
            if (player == g.getColor() && g.getSize() > out){
                out = g.getSize();
            }
        }
        return out;
    }
    
    public int getNumRegions(int player){
        int out = 0;
        for (Group region : theRegionList) {
            boolean blackAdj = false;
            boolean whiteAdj = false;
            MoveList stones = region.getStoneList();
            for (Move stone : stones) {
                if (blackStoneAdjacent(stone)){
                    blackAdj = true;
                }
                if (whiteStoneAdjacent(stone)){
                    whiteAdj = true;
                }
            }
            if (blackAdj && !whiteAdj && player == BLACK){
                out ++;
            }
            else if (!blackAdj && whiteAdj && player == WHITE){
                out ++;
            }
        }
        return out;
    }
    
    // returns the number of liberties of the player's groups
    int getLiberties(int player){
        int out = 0;
        for (Group g : theGroupList) {
            if (g.getColor() == player){
                out += g.getLiberties();
            }
        }
        return out;
    }
    
    void setSuppressPrint(boolean b){
        suppressPrint = b;
    }
    
    @Override
    public boolean equals(Object o){
        boolean out = true;
        Board b = (Board) o;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (b.board[i][j] != this.board[i][j]){
                    out = false;
                }
            }
        }
        return out;
    }
    
    public static void main(String[] args) {
//        Group g = new Group();
//        System.out.println(g.getLiberties());
        Board b = new Board(5);
        
        b.tryMove(new Move(1,1));
        b.pass();
        b.tryMove(new Move(1,3));
        b.pass();
        b.tryMove(new Move(0,2));
        b.pass();
        b.tryMove(new Move(2,2));
        b.printBoard();
        b.generateGroups();
        b.generateRegions();
        System.out.println(b.theRegionList.get(1));
        System.out.println(b.theGroupList.size());
    }
}
