/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;

/**
 * In case I need it, this Node contains a move and its corresponding board and score
 * Not sure how/whether nodes/moves will be necessary for generalized learning, so will find out later
 * Not currently in use
 * @author tvtuttle
 */
public class Node {
    int score;
    Board boardState;
    
    public Node(){
        score = 0;
        boardState = new Board();
    }
    public Node(int s, Board b){
        score = s;
        boardState = b;
    }
}
