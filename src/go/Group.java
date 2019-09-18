/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;
import java.awt.*;
/**
 * concept taken from levenick
 * @author tvtuttle
 */
public class Group {
    Board theBoard;
    private int size;
    int liberties;
    boolean[][] counted;
    MoveList stoneList;
    int color;
    
    int status;
    
    static final int ALIVE = 1;
    static final int DEAD = 2;
    static final int UNKNOWN = 0;
    
    public Group(int col, int row, Board b){
        theBoard = b;
        size = theBoard.getSize();
        color = theBoard.board[col][row];
        stoneList = new MoveList();
        stoneList.add(new Move(col, row));
        liberties = 0;
    }
    
    void removeStones(Board b){
        for (int i = 0; i < stoneList.size(); i++) {
            Move theStone = stoneList.get(i);
            b.setEmpty(theStone);
        }
    }
    // the foo methods fill areas with "foo", a value that equals 100
    // no idea what they are for? i think its just for debugging?
    
    boolean includes(int c, int r){
        for (int i = 0; i < stoneList.size(); i++) {
            Move theStone = stoneList.get(i);
            int col = theStone.getX();
            int row = theStone.getY();
            if (r==row&&c==col){
                return true;
            }
        }
        return false;
    }
    
    public void addStone(int col, int row){
        stoneList.add(new Move(col, row));
        
    }
    public boolean sameColor(int otherColor){
        return color==otherColor;
    }
    
    public int countLiberties(){
//        System.out.println("here");
        counted = new boolean[size][size]; //initialize as false
        liberties = 0;
        for (int i = 0; i < stoneList.size(); i++) {
//            System.out.println(stoneList.get(i));
            liberties += addLiberties(stoneList.get(i));
        }
        return liberties;
    }
    int addLiberties(Move m){
        int col = m.getX();
        int row = m.getY();
        return more(col, row+1)+more(col,row-1)+more(col+1,row)+more(col-1,row);
    }
    int more(int col, int row){
        if (!theBoard.on(row) || !theBoard.on(col)
                || (counted[col][row])
                || (theBoard.board[col][row] != theBoard.EMPTY)){
//            System.out.println("no liberty");
            return 0;
        }
        // if its on board and not counted and empty, its good
        counted[col][row]=true;
//        System.out.println("liberty counted");
        return 1;
    }
    
    // foo methods, used for score counting
    void fillWithFoo(){
        for (int i = 0; i < stoneList.size(); i++) {
            Move stone = stoneList.get(i);
            theBoard.setFoo(stone);
        }
    }
    void unFillWithFoo(){
        for (int i = 0; i < stoneList.size(); i++) {
            Move stone = stoneList.get(i);
            theBoard.setFoo(stone);
        }
    }
    
    void setStatus(int s){status=s;}
    int getStatus(){return status;}
    int getNumStones(){return stoneList.size();}
    int getColor(){return color;}
    int getLiberties(){
//        System.out.println("right r");
        countLiberties(); 
//        System.out.println("left r");
        return liberties;
    }
    int getSize(){return stoneList.size();}
    MoveList getStoneList(){return stoneList;}
    
    @Override
    public String toString(){
        String out = "";
        for (Move stone : stoneList) {
            out += stone.toString();
            out += "\n";
        }
        return out;
    }
}
