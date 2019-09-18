/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;

/**
 * A simple scoring algorithm, that does not consider life or death
 * Currently in use
 * @author tvtuttle
 */
public class ScoreCounterSimple {
    Board theBoard;
    //area scoring:
    // num points at end is num stones and empty space fully surrounded
    // life and death are NOT considered, making this way simpler
    public ScoreCounterSimple(Board b){
        theBoard = b;
    }
    
    int countScore(){
        int black = 0;
        int white = 0;
        // find empty spaces fully surrounded by stones
        black += theBoard.blackCaptures;
        white += theBoard.whiteCaptures;
        theBoard.generateGroups();
        theBoard.generateRegions();
        for (Group region : theBoard.theRegionList) {
            boolean blackAdj = false;
            boolean whiteAdj = false;
            MoveList stones = region.getStoneList();
            for (Move stone : stones) {
                if (theBoard.blackStoneAdjacent(stone)){
                    blackAdj = true;
                }
                if (theBoard.whiteStoneAdjacent(stone)){
                    whiteAdj = true;
                }
            }
            if (blackAdj && !whiteAdj){
                black += region.getSize();
            }
            else if (whiteAdj && !blackAdj){
                white += region.getSize();
            }
        }
        return white-black;
    }
    
}
