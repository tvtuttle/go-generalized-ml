/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;

/**
 * Based on JRLScoreCounter by levenick
 * Not currently in use and nonfunctional
 * @author tvtuttle
 */
public class ScoreCounter {
    boolean debug = false;
    Board theBoard;
    String outcome = "?";
    GroupList blackSurroundList, whiteSurroundList, mixedSurroundList;
    public ScoreCounter(Board b){
        theBoard = b;
    }
    
    public void countScore(){
        int count = countEm();
        if (count > 0){
            outcome = "white wins by" + count;
        }
        else if (count < 0){
            outcome = "black wins by" + count;
        }
        else outcome = "jigo";
        System.out.println(outcome);
    }
    public int countEm(){
        Board tempBoard = theBoard.myClone();
        classifyRegions(tempBoard);
        
        int wTerritory = whiteSurroundList.countTerritory();
        int bTerritory = blackSurroundList.countTerritory();
        removePris(tempBoard);
        
        if (tempBoard.getCapturedList().isEmpty()){
            return ((theBoard.getWhiteCaptures()+wTerritory)-
                    (theBoard.getBlackCaptures()+bTerritory));
        }
        else {
            tempBoard.getCapturedList().removeStones(tempBoard);
            theBoard.generateGroups();
            theBoard.setWhiteCaptures(tempBoard.getWhiteCaptures());
            theBoard.setBlackCaptures(tempBoard.getBlackCaptures());
            return countEm();
        }
    }
    
    void classifyRegions(Board b){
        GroupList regionList = b.theRegionList;
        mixedSurroundList = new GroupList();
        whiteSurroundList = new GroupList();
        blackSurroundList = new GroupList();
        for (Group g : regionList) {
            boolean blackAdjacent = false;
            boolean whiteAdjacent = false;
            for (Move m : g.getStoneList()) {
                if (b.blackStoneAdjacent(m))
                    blackAdjacent = true;
                if (b.whiteStoneAdjacent(m))
                    whiteAdjacent = true;
            }
            if (blackAdjacent && whiteAdjacent)
                mixedSurroundList.add(g);
            else if (whiteAdjacent)
                whiteSurroundList.add(g);
            else
                blackSurroundList.add(g);
        }
    }
    
    void removePris(Board b){
        //remove randomly scattered stones, then groups with <2 eyes
        removeRandomScatteredStones(b);
        removeDeadGroups(b);
    }
    
    void removeRandomScatteredStones(Board b){
        for (Group g : mixedSurroundList) {
            // fill region specified
            MoveList stones = g.getStoneList();
            boolean[] played = new boolean[stones.size()];
            int noMoveCount = 0;
            while(noMoveCount < 2 && countSpacesLeft(played)>0){
                int moveIndex = findMove(b, stones);
                if (moveIndex != -1){
                    int numCaptures = b.makeMoveNoKo(stones.get(moveIndex));
                    played[moveIndex] = true;
                    noMoveCount = 0;
                    if (numCaptures > 0){
                        if (b.whoseTurn == Board.BLACK)
                            b.whiteCaptures += numCaptures;
                        else
                            b.blackCaptures += numCaptures;
                    }
                }
                else {
                    noMoveCount ++;
                    b.switchTurn();
                }
            }
        }
    }
    int findMove(Board localBoard, MoveList emptySpots){
//        int out = -1;
        for (int i = 0; i < emptySpots.size(); i++) {
            Move m = emptySpots.get(i);
            if (localBoard.board[m.getX()][m.getY()] == Board.EMPTY
                    && localBoard.stoneAdjacent(m, localBoard.whoseTurn)
                    && atLeast2Liberties(localBoard, m))
                return i;
        }
        return -1;//nothing
    }
    int countSpacesLeft(boolean[] played){
        int out = 0;
        for (boolean b : played) {
            if (!b)
                out++;
        }
        return out;
    }
    boolean atLeast2Liberties(Board b, Move m){
        if (b.legalMove(m)){
            Board temp = (Board) b.clone();
            temp.theScoreCounter = null;
            temp.generateGroups();
            temp.makeMoveNoKo(m);
            return temp.groups[m.getX()][m.getY()].getLiberties()>= 2;
        }
        return false;
    }
    
    void removeDeadGroups(Board b){
        b.generateGroups();
        b.generateRegions();
        classifyRegions(b);
        checkStatus(b);
        GroupList gList = b.theGroupList;
        for (Group g : gList) {
            if (g.getStatus() == Group.DEAD){
//                System.out.println("wait what");
                int captures = b.removeGroup(g);
                if (g.getColor() == Board.WHITE){
                    b.blackCaptures += captures;
                }
                else {
                    b.whiteCaptures += captures;
                }
            }
        }
    }
    // mimics what levenick does when initializing lifereader
    void checkStatus(Board b){ //also sets status
//        GroupList rList = b.theRegionList;
        GroupList gList = b.theGroupList;
        GroupList theSurroundList = null;
        boolean out = true;
        for (Group g : gList) {
            if (hasManyEyes(g)){
                g.setStatus(Group.ALIVE);
            }
            else {
                g.setStatus(Group.DEAD);
            }
        }
    }
    
    // true if num eyes >= 2
    boolean hasManyEyes(Group g){
        GroupList theSurroundList = null;
        if (g.getColor() == Board.WHITE){
            theSurroundList = whiteSurroundList;
        }
        else {
            theSurroundList = blackSurroundList;
        }
        for (Group region : theSurroundList) {
            region.fillWithFoo();
            if (g.countLiberties() == 0){
                region.unFillWithFoo();
                return false;
            }
            region.unFillWithFoo();
        }
        
        return true;
    }
}
