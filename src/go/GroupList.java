/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;

import java.util.ArrayList;

/**
 *
 * @author tvtuttle
 */
public class GroupList extends ArrayList<Group>{
    public void countLiberties(){
        for (Group thi : this) {
            thi.countLiberties();
        }
    }
    public int countTerritory(){
        int count=0;
        for (Group g: this){
            count += g.getStoneList().size();
        }
        return count;
    }
    public void removeStones(Board b){
        for (Group thi : this) {
            thi.removeStones(b);
        }
    }
}
