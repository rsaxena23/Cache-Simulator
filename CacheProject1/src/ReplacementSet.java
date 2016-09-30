import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

/**
 * Created by Raunaq-PC on 29-09-2016.
 */
public class ReplacementSet {
    int[][] replData;
    int replacementPolicy;
    int systemTimer=0;
    public ReplacementSet(int replacementPolicy,int noOfSets,int associativity)
    {
        this.replacementPolicy=replacementPolicy;
        if(replacementPolicy == Constants.LRU)
            replData = new int[noOfSets][associativity];
        else if(replacementPolicy == Constants.FIFO)
            replData = new int[noOfSets][1];
        /*else if(replacementPolicy == Constants.PSEUDO_LRU)*/
    }
    public int getReplaceIndex(int index)
    {
        int i;
        /* LRU Case*/
        if(replacementPolicy== Constants.LRU)
        {
            int min = replData[index][0];
            int replace_index=0;
            for(i=1;i<replData[index].length;i++) {
                if (replData[index][i] < min) {
                    min = replData[index][i];
                    replace_index=i;
                }
            }
            replData[index][replace_index] = ++systemTimer;
            return replace_index;
        }

        /* FIFO Case */
        else if(replacementPolicy==Constants.FIFO) {
            return replData[index][0]++;
        }

        /*Pseudo LRU Case */
        return -1;
    }
}
